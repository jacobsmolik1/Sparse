package com.sparse.launcher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.OnBackgroundDim

@Composable
fun AppSettingsScreen(vm: LauncherViewModel, app: AppInfo) {
    val prefs = vm.prefs
    val pinnedApps by vm.pinnedApps.collectAsState()
    val isPinned = pinnedApps.any { it.packageName == app.packageName }
    var isMindful by remember { mutableStateOf(app.packageName in prefs.mindfulApps) }
    var isFocusBlocked by remember { mutableStateOf(app.packageName in prefs.focusBlockedApps) }
    var labelText by remember { mutableStateOf(prefs.getCustomLabel(app.packageName) ?: "") }
    val dailyLimit = remember { mutableIntStateOf(prefs.getDailyLimit(app.packageName)) }
    val opensToday = remember { vm.usage.getOpensToday(app.packageName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Column {
            Text(
                text = app.label,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = app.packageName,
                fontSize = 11.sp,
                color = OnBackgroundDim,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("pin to home", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Switch(
                checked = isPinned,
                onCheckedChange = { checked ->
                    if (checked) vm.pinApp(app.packageName)
                    else vm.unpinApp(app.packageName)
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("mindful pause", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("3s delay before opening", fontSize = 12.sp, color = OnBackgroundDim)
            }
            Switch(
                checked = isMindful,
                onCheckedChange = { checked ->
                    isMindful = checked
                    vm.toggleMindful(app.packageName)
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("block in focus mode", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("blocked when focus is on", fontSize = 12.sp, color = OnBackgroundDim)
            }
            Switch(
                checked = isFocusBlocked,
                onCheckedChange = { checked ->
                    isFocusBlocked = checked
                    vm.toggleFocusBlocked(app.packageName)
                },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("custom label", fontSize = 14.sp, color = OnBackgroundDim)
            BasicTextField(
                value = labelText,
                onValueChange = { labelText = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (labelText.isEmpty()) {
                        Text(app.label, fontSize = 16.sp, color = OnBackgroundDim)
                    }
                    inner()
                },
            )
        }

        Text(
            text = "opened today: $opensToday",
            fontSize = 14.sp,
            color = OnBackgroundDim,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = { vm.back() }) {
                Text("cancel", color = OnBackgroundDim)
            }
            TextButton(onClick = {
                vm.setCustomLabel(app.packageName, labelText)
                vm.back()
            }) {
                Text("save", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
