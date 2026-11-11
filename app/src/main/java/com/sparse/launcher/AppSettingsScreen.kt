package com.sparse.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.LocalSparseFont
import com.sparse.launcher.ui.theme.dimContent

@Composable
fun AppSettingsScreen(vm: LauncherViewModel, app: AppInfo) {
    val prefs = vm.prefs
    val font = LocalSparseFont.current
    val pinnedApps by vm.pinnedApps.collectAsState()
    val isPinned = pinnedApps.any { it.packageName == app.packageName }
    var isMindful by remember { mutableStateOf(app.packageName in prefs.mindfulApps) }
    var isFocusBlocked by remember { mutableStateOf(app.packageName in prefs.focusBlockedApps) }
    var isHidden by remember { mutableStateOf(app.packageName in prefs.hiddenApps) }
    var labelText by remember { mutableStateOf(prefs.getCustomLabel(app.packageName) ?: "") }
    val opensToday = remember { vm.usage.getOpensToday(app.packageName) }
    var dailyLimit by remember { mutableIntStateOf(prefs.getDailyLimit(app.packageName)) }

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
                fontFamily = font,
            )
            Text(
                text = app.packageName,
                fontSize = 11.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "pin to home",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = font,
            )
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
                Text(
                    text = "mindful pause",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                )
                Text(
                    text = "3s delay before opening",
                    fontSize = 12.sp,
                    color = MaterialTheme.dimContent,
                    fontFamily = font,
                )
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
                Text(
                    text = "block in focus mode",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                )
                Text(
                    text = "blocked when focus is on",
                    fontSize = 12.sp,
                    color = MaterialTheme.dimContent,
                    fontFamily = font,
                )
            }
            Switch(
                checked = isFocusBlocked,
                onCheckedChange = { checked ->
                    isFocusBlocked = checked
                    vm.toggleFocusBlocked(app.packageName)
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "hidden from drawer",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = font,
            )
            Switch(
                checked = isHidden,
                onCheckedChange = { checked ->
                    isHidden = checked
                    if (checked) vm.hideApp(app.packageName)
                    else vm.unhideApp(app.packageName)
                },
            )
        }

        // Daily limit row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "daily limit",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = font,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "-",
                    fontSize = 20.sp,
                    color = if (dailyLimit > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.dimContent,
                    fontFamily = font,
                    modifier = Modifier.clickable {
                        if (dailyLimit > 0) {
                            dailyLimit -= 1
                            prefs.setDailyLimit(app.packageName, dailyLimit)
                        }
                    },
                )
                Text(
                    text = if (dailyLimit == 0) "no limit" else "$dailyLimit opens",
                    fontSize = 14.sp,
                    color = MaterialTheme.dimContent,
                    fontFamily = font,
                )
                Text(
                    text = "+",
                    fontSize = 20.sp,
                    color = if (dailyLimit < 50) MaterialTheme.colorScheme.onBackground else MaterialTheme.dimContent,
                    fontFamily = font,
                    modifier = Modifier.clickable {
                        if (dailyLimit < 50) {
                            dailyLimit += 1
                            prefs.setDailyLimit(app.packageName, dailyLimit)
                        }
                    },
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "custom label",
                fontSize = 14.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
            BasicTextField(
                value = labelText,
                onValueChange = { labelText = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (labelText.isEmpty()) {
                        Text(
                            text = app.label,
                            fontSize = 16.sp,
                            color = MaterialTheme.dimContent,
                            fontFamily = font,
                        )
                    }
                    inner()
                },
            )
        }

        Text(
            text = "opened today: $opensToday",
            fontSize = 14.sp,
            color = MaterialTheme.dimContent,
            fontFamily = font,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = { vm.back() }) {
                Text(
                    text = "cancel",
                    color = MaterialTheme.dimContent,
                    fontFamily = font,
                )
            }
            TextButton(onClick = {
                vm.setCustomLabel(app.packageName, labelText)
                vm.back()
            }) {
                Text(
                    text = "save",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                )
            }
        }
    }
}
