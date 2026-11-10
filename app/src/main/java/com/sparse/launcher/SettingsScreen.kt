package com.sparse.launcher

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.OnBackgroundDim

@Composable
fun SettingsScreen(vm: LauncherViewModel) {
    var focusMode by remember { mutableStateOf(vm.prefs.focusModeEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Text(
            "settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("focus mode", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("blocks flagged apps", fontSize = 12.sp, color = OnBackgroundDim)
            }
            Switch(
                checked = focusMode,
                onCheckedChange = { checked ->
                    focusMode = checked
                    vm.prefs.focusModeEnabled = checked
                },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("gestures", fontSize = 14.sp, color = OnBackgroundDim)
            Text("swipe up → app drawer", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("swipe down → notifications", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("long press → settings", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("long press app → app settings", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        TextButton(onClick = { vm.back() }) {
            Text("done", color = OnBackgroundDim)
        }
    }
}
