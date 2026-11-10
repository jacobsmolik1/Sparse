package com.sparse.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.BG_COLORS
import com.sparse.launcher.ui.theme.BG_COLOR_NAMES
import com.sparse.launcher.ui.theme.TEXT_COLORS
import com.sparse.launcher.ui.theme.TEXT_COLOR_NAMES
import com.sparse.launcher.ui.theme.dimContent

@Composable
fun SettingsScreen(vm: LauncherViewModel) {
    val theme by vm.themeState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        Text(
            "settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Home screen apps
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("home screen apps")
            TextButton(
                onClick = { vm.navigate(Screen.ManageHomeApps) },
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    "manage apps →",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                "pick and reorder the apps shown on your home screen",
                fontSize = 13.sp,
                color = MaterialTheme.dimContent,
            )
        }

        // Clock format
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("clock format")
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClockFormatOption(label = "24h", selected = theme.clock24h) {
                    vm.setClock24h(true)
                }
                ClockFormatOption(label = "12h", selected = !theme.clock24h) {
                    vm.setClock24h(false)
                }
            }
        }

        // Background color
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionLabel("background")
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                BG_COLORS.forEachIndexed { i, color ->
                    ColorSwatch(
                        color = color,
                        label = BG_COLOR_NAMES[i],
                        selected = i == theme.bgColorIndex,
                        onClick = { vm.setBgColor(i) },
                    )
                }
            }
        }

        // Text color
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionLabel("text color")
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TEXT_COLORS.forEachIndexed { i, color ->
                    ColorSwatch(
                        color = color,
                        label = TEXT_COLOR_NAMES[i],
                        selected = i == theme.textColorIndex,
                        onClick = { vm.setTextColor(i) },
                    )
                }
            }
        }

        // Focus mode
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("focus mode")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var focusMode by remember { mutableStateOf(vm.prefs.focusModeEnabled) }
                Column {
                    Text("enabled", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("blocks flagged apps", fontSize = 12.sp, color = MaterialTheme.dimContent)
                }
                Switch(
                    checked = focusMode,
                    onCheckedChange = { checked ->
                        focusMode = checked
                        vm.prefs.focusModeEnabled = checked
                    },
                )
            }
        }

        // Gestures hint
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("gestures")
            listOf(
                "swipe up → app drawer",
                "swipe down → notifications",
                "long press home → settings",
                "long press app → app settings",
            ).forEach {
                Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        TextButton(onClick = { vm.back() }, contentPadding = PaddingValues(0.dp)) {
            Text("done", color = MaterialTheme.dimContent)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 12.sp, color = MaterialTheme.dimContent, letterSpacing = 1.sp)
}

@Composable
private fun ClockFormatOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.dimContent
    Text(
        text = label,
        fontSize = 18.sp,
        fontWeight = if (selected) FontWeight.Normal else FontWeight.Light,
        color = color,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun ColorSwatch(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
    )
}
