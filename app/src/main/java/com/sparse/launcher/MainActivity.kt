package com.sparse.launcher

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sparse.launcher.ui.theme.BG_COLORS
import com.sparse.launcher.ui.theme.TEXT_COLORS
import com.sparse.launcher.ui.theme.SparseTheme

private fun tryLockScreen(ctx: Context) {
    val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val admin = ComponentName(ctx, SparseDeviceAdmin::class.java)
    if (dpm.isAdminActive(admin)) {
        dpm.lockNow()
    } else {
        ctx.startActivity(
            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Allows double-tap on home screen to lock the screen")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SparseApp()
        }
    }
}

@Composable
private fun SparseApp() {
    val vm: LauncherViewModel = viewModel()
    val screen by vm.screen.collectAsState()
    val theme by vm.themeState.collectAsState()
    val ctx = LocalContext.current

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        val ctrl = WindowInsetsControllerCompat(window, view)
        if (theme.hideStatusBar) {
            ctrl.hide(WindowInsetsCompat.Type.statusBars())
            ctrl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            ctrl.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    val fontFamily = when (theme.fontFamily) {
        "serif" -> FontFamily.Serif
        "mono" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }

    SparseTheme(
        bgColor = BG_COLORS[theme.bgColorIndex],
        textColor = TEXT_COLORS[theme.textColorIndex],
        fontFamily = fontFamily,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            BackHandler(enabled = screen != Screen.Home) { vm.back() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (screen == Screen.Home) Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = { tryLockScreen(ctx) },
                                onLongPress = { vm.navigate(Screen.Settings) },
                            )
                        } else Modifier
                    )
            ) {
                when (val s = screen) {
                    is Screen.Home -> HomeScreen(vm)
                    is Screen.Drawer -> DrawerScreen(vm)
                    is Screen.Settings -> SettingsScreen(vm)
                    is Screen.ManageHomeApps -> ManageHomeAppsScreen(vm)
                    is Screen.HiddenApps -> HiddenAppsScreen(vm)
                    is Screen.Gate -> GateScreen(vm, s.app, s.reason)
                    is Screen.AppSettings -> AppSettingsScreen(vm, s.app)
                    is Screen.PickSwipeApp -> PickSwipeAppScreen(vm, s.direction)
                    is Screen.PickApp -> PickAppScreen(vm, s.target)
                }
            }
        }
    }
}
