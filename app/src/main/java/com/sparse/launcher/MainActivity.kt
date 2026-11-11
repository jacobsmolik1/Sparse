package com.sparse.launcher

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

            Box(modifier = Modifier.fillMaxSize()) {
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
