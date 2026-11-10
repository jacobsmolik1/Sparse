package com.sparse.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sparse.launcher.ui.theme.SparseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SparseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SparseApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SparseApp() {
    val vm: LauncherViewModel = viewModel()
    val screen by vm.screen.collectAsState()

    BackHandler(enabled = screen != Screen.Home) {
        vm.back()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .run {
                if (screen == Screen.Home) {
                    combinedClickable(
                        onClick = {},
                        onLongClick = { vm.navigate(Screen.Settings) },
                    )
                } else this
            }
    ) {
        when (val s = screen) {
            is Screen.Home -> HomeScreen(vm)
            is Screen.Drawer -> DrawerScreen(vm)
            is Screen.Settings -> SettingsScreen(vm)
            is Screen.Gate -> GateScreen(vm, s.app)
            is Screen.AppSettings -> AppSettingsScreen(vm, s.app)
        }
    }
}
