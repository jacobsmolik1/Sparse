package com.sparse.launcher

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparse.launcher.ui.theme.BG_COLORS
import com.sparse.launcher.ui.theme.BG_COLOR_NAMES
import com.sparse.launcher.ui.theme.LocalSparseFont
import com.sparse.launcher.ui.theme.TEXT_COLORS
import com.sparse.launcher.ui.theme.TEXT_COLOR_NAMES
import com.sparse.launcher.ui.theme.dimContent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: LauncherViewModel) {
    val theme by vm.themeState.collectAsState()
    val font = LocalSparseFont.current
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        Text(
            text = "settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = font,
        )

        // Home screen apps
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("home screen apps", font)
            TextButton(
                onClick = { vm.navigate(Screen.ManageHomeApps) },
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = "manage apps →",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                )
            }
            Text(
                text = "pick and reorder the apps shown on your home screen",
                fontSize = 13.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        }

        // Font section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("font", font)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                listOf("default", "serif", "mono").forEach { opt ->
                    ClockFormatOption(label = opt, selected = theme.fontFamily == opt, font = font) {
                        vm.setFontFamily(opt)
                    }
                }
            }
        }

        // Show / hide section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("show / hide", font)
            listOf(
                "clock" to theme.showClock,
                "date" to theme.showDate,
                "weather" to theme.showWeather,
                "now playing" to theme.showNowPlaying,
                "calendar" to theme.showCalendar,
                "status bar" to !theme.hideStatusBar,
            ).forEach { (label, checked) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = font,
                    )
                    Switch(
                        checked = checked,
                        onCheckedChange = { v ->
                            when (label) {
                                "clock" -> vm.setShowClock(v)
                                "date" -> vm.setShowDate(v)
                                "weather" -> vm.setShowWeather(v)
                                "now playing" -> vm.setShowNowPlaying(v)
                                "calendar" -> vm.setShowCalendar(v)
                                "status bar" -> vm.setHideStatusBar(!v)
                            }
                        },
                    )
                }
            }
        }

        // Clock format
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("clock format", font)
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClockFormatOption(label = "24h", selected = theme.clock24h, font = font) {
                    vm.setClock24h(true)
                }
                ClockFormatOption(label = "12h", selected = !theme.clock24h, font = font) {
                    vm.setClock24h(false)
                }
            }
        }

        // Temperature unit
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("temperature", font)
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClockFormatOption(label = "°F", selected = theme.useFahrenheit, font = font) {
                    vm.setUseFahrenheit(true)
                }
                ClockFormatOption(label = "°C", selected = !theme.useFahrenheit, font = font) {
                    vm.setUseFahrenheit(false)
                }
            }
        }

        // Clock size
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("clock size", font)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                listOf("small", "medium", "large").forEach { opt ->
                    ClockFormatOption(label = opt, selected = theme.clockSize == opt, font = font) {
                        vm.setClockSize(opt)
                    }
                }
            }
        }

        // Body text size
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("text size", font)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                listOf("small", "medium", "large").forEach { opt ->
                    ClockFormatOption(label = opt, selected = theme.bodySize == opt, font = font) {
                        vm.setBodySize(opt)
                    }
                }
            }
        }

        // Text alignment
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("text alignment", font)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                listOf("left", "center", "right").forEach { opt ->
                    ClockFormatOption(label = opt, selected = theme.textAlign == opt, font = font) {
                        vm.setTextAlign(opt)
                    }
                }
            }
        }

        // Vertical layout
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("vertical layout", font)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                listOf("spread", "top", "center", "bottom").forEach { opt ->
                    ClockFormatOption(label = opt, selected = theme.verticalLayout == opt, font = font) {
                        vm.setVerticalLayout(opt)
                    }
                }
            }
            Text(
                text = "spread: clock top, apps bottom",
                fontSize = 12.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        }

        // Tap targets section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("tap actions", font)
            val allAppsVal = vm.allApps.collectAsState().value
            listOf(
                "clock" to (allAppsVal.find { it.packageName == theme.clockTapPackage }?.displayLabel ?: "none"),
                "weather" to (allAppsVal.find { it.packageName == theme.weatherTapPackage }?.displayLabel ?: "none"),
                "music" to (allAppsVal.find { it.packageName == theme.musicTapPackage }?.displayLabel ?: "none"),
                "calendar" to (allAppsVal.find { it.packageName == theme.calendarTapPackage }?.displayLabel ?: "none"),
            ).forEach { (label, appLabel) ->
                val target = when (label) {
                    "clock" -> "clockTap"
                    "weather" -> "weatherTap"
                    "music" -> "musicTap"
                    else -> "calendarTap"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, fontFamily = font)
                    Text(
                        text = appLabel,
                        fontSize = 16.sp,
                        color = MaterialTheme.dimContent,
                        fontFamily = font,
                        modifier = Modifier.clickable { vm.navigate(Screen.PickApp(target)) },
                    )
                }
            }
        }

        // Swipe gestures section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("swipe gestures", font)
            val swipeLeftLabel = vm.allApps.collectAsState().value
                .find { it.packageName == theme.swipeLeftPackage }?.displayLabel ?: "none"
            val swipeRightLabel = vm.allApps.collectAsState().value
                .find { it.packageName == theme.swipeRightPackage }?.displayLabel ?: "none"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "left →",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                )
                Text(
                    text = swipeLeftLabel,
                    fontSize = 16.sp,
                    color = MaterialTheme.dimContent,
                    fontFamily = font,
                    modifier = Modifier.clickable { vm.navigate(Screen.PickSwipeApp("left")) },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "right →",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                )
                Text(
                    text = swipeRightLabel,
                    fontSize = 16.sp,
                    color = MaterialTheme.dimContent,
                    fontFamily = font,
                    modifier = Modifier.clickable { vm.navigate(Screen.PickSwipeApp("right")) },
                )
            }
        }

        // Hidden apps section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("hidden apps", font)
            val hiddenCount = remember(vm.prefs.hiddenApps) { vm.prefs.hiddenApps.size }
            TextButton(
                onClick = { vm.navigate(Screen.HiddenApps) },
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = "manage →",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                )
            }
            Text(
                text = "$hiddenCount apps hidden",
                fontSize = 13.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        }

        // Background color
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionLabel("background", font)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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
            SectionLabel("text color", font)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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
            SectionLabel("focus mode", font)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var focusMode by remember { mutableStateOf(vm.prefs.focusModeEnabled) }
                Column {
                    Text(
                        text = "enabled",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = font,
                    )
                    Text(
                        text = "blocks flagged apps",
                        fontSize = 12.sp,
                        color = MaterialTheme.dimContent,
                        fontFamily = font,
                    )
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

        // Music widget section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel("music widget", font)
            Text(
                text = "supports Spotify and YouTube Music",
                fontSize = 13.sp,
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
            val isListenerEnabled = remember { NowPlayingStore.isListenerEnabled(ctx) }
            if (!isListenerEnabled) {
                Text(
                    text = "enable notification access →",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                    modifier = Modifier.clickable {
                        ctx.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                )
            }
        }

        // Gestures hint
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("gestures", font)
            listOf(
                "swipe up → app drawer",
                "swipe down → notifications",
                "double-tap home → sleep",
                "long press home → settings",
                "long press app → app settings",
            ).forEach {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = font,
                )
            }
        }

        TextButton(onClick = { vm.back() }, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = "done",
                color = MaterialTheme.dimContent,
                fontFamily = font,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, font: androidx.compose.ui.text.font.FontFamily) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = MaterialTheme.dimContent,
        letterSpacing = 1.sp,
        fontFamily = font,
    )
}

@Composable
private fun ClockFormatOption(
    label: String,
    selected: Boolean,
    font: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit,
) {
    val color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.dimContent
    Text(
        text = label,
        fontSize = 18.sp,
        fontWeight = if (selected) FontWeight.Normal else FontWeight.Light,
        color = color,
        fontFamily = font,
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
