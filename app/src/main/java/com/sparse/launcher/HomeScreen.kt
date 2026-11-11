package com.sparse.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.sparse.launcher.ui.theme.LocalSparseFont
import com.sparse.launcher.ui.theme.dimContent
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(vm: LauncherViewModel) {
    val ctx = LocalContext.current
    val pinnedApps by vm.pinnedApps.collectAsState()
    val theme by vm.themeState.collectAsState()
    val weatherState by vm.weatherState.collectAsState()
    val calendarEvents by vm.calendarEvents.collectAsState()
    val notifPackages by NotificationStore.active.collectAsState()
    val font = LocalSparseFont.current

    var time by remember { mutableStateOf(currentTimeStr(theme.clock24h)) }
    var date by remember { mutableStateOf(currentDateStr()) }
    LaunchedEffect(theme.clock24h) {
        while (true) {
            time = currentTimeStr(theme.clock24h)
            date = currentDateStr()
            delay(10_000)
        }
    }

    val horizAlign = theme.textAlign.toColumnAlign()
    val composeTextAlign = theme.textAlign.toTextAlign()
    val vertArrangement = theme.verticalLayout.toArrangement()
    val clockSp = theme.clockSize.toClockSp()
    val bodySp = theme.bodySize.toBodySp()

    // Permission launcher — used only on first ask
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) {
            getLastLocation(ctx)?.let { (lat, lon) ->
                vm.updateLocation(lat, lon)
                vm.fetchWeather()
            } ?: vm.setWeatherError()
        } else {
            vm.setWeatherError()
        }
    }

    // First-time: request permission or fetch
    LaunchedEffect(Unit) {
        if (hasLocationPermission(ctx)) {
            getLastLocation(ctx)?.let { (lat, lon) ->
                vm.updateLocation(lat, lon)
                vm.fetchWeather()
            } ?: vm.setWeatherError()
        } else if (!vm.weatherPermissionDenied) {
            permLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ))
        }
    }

    // Refresh on every resume (repo cache prevents over-fetching)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasLocationPermission(ctx)) {
            getLastLocation(ctx)?.let { (lat, lon) ->
                vm.updateLocation(lat, lon)
                vm.refreshWeather()
            }
        }
        vm.refreshCalendar(ctx)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(theme.swipeLeftPackage, theme.swipeRightPackage) {
                var lastTapMs = 0L
                awaitEachGesture {
                    // Main pass fires AFTER children — so if a child (app label) consumed
                    // the down, down.isConsumed will be true here.
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // Do NOT consume — let children handle their own taps.
                    var drag = Offset.Zero
                    var released = false

                    // Phase 1: collect movement until release, with long-press timeout.
                    val timedOut = withTimeoutOrNull(600L) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val c = event.changes.firstOrNull { it.id == down.id } ?: break
                            drag += c.positionChange()
                            if (!c.pressed) { released = true; break }
                            // Break early once drag is unambiguous — no need to wait for timeout
                            if (drag.getDistance() > viewConfiguration.touchSlop * 3) break
                        }
                    }
                    val longPress = timedOut == null && !released &&
                        drag.getDistance() < viewConfiguration.touchSlop

                    // Phase 2: if still pressed after early exit, collect the rest.
                    if (!released && !longPress) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val c = event.changes.firstOrNull { it.id == down.id } ?: break
                            drag += c.positionChange()
                            if (!c.pressed) { released = true; break }
                        }
                    }

                    val adx = abs(drag.x)
                    val ady = abs(drag.y)
                    when {
                        longPress -> {
                            if (!down.isConsumed) vm.navigate(Screen.Settings)
                            // drain until release
                            while (true) {
                                val e = awaitPointerEvent()
                                if (e.changes.none { it.id == down.id && it.pressed }) break
                            }
                        }
                        ady >= 80f && ady >= adx * 2f -> {
                            if (drag.y < 0f) vm.navigate(Screen.Drawer)
                            else expandNotifications(ctx)
                        }
                        adx >= 100f && adx >= ady * 2f -> {
                            if (drag.x < 0f) theme.swipeLeftPackage?.let { vm.launchByPackage(it) }
                            else theme.swipeRightPackage?.let { vm.launchByPackage(it) }
                        }
                        else -> {
                            // Tap on empty space only (child consumed = app tap, skip)
                            if (!down.isConsumed) {
                                val now = System.currentTimeMillis()
                                if (now - lastTapMs < 500L) {
                                    tryLockScreen(ctx)
                                    lastTapMs = 0L
                                } else {
                                    lastTapMs = now
                                }
                            } else {
                                lastTapMs = 0L
                            }
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            verticalArrangement = vertArrangement,
            horizontalAlignment = horizAlign,
        ) {
            // Clock / date / weather / now playing block
            Column(horizontalAlignment = horizAlign) {
                if (theme.showClock) {
                    Text(
                        text = time,
                        fontSize = clockSp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = clockSp * 1.05,
                        textAlign = composeTextAlign,
                        fontFamily = font,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (theme.clockTapPackage != null)
                                    Modifier.clickable { vm.launchByPackage(theme.clockTapPackage!!) }
                                else Modifier
                            ),
                    )
                }
                if (theme.showDate) {
                    Text(
                        text = date,
                        fontSize = bodySp * 0.88f,
                        color = MaterialTheme.dimContent,
                        textAlign = composeTextAlign,
                        fontFamily = font,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .then(
                                if (theme.calendarTapPackage != null)
                                    Modifier.clickable { vm.launchByPackage(theme.calendarTapPackage!!) }
                                else Modifier
                            ),
                    )
                }
                if (theme.showWeather) {
                    WeatherLine(
                        state = weatherState,
                        textAlign = composeTextAlign,
                        bodySp = bodySp,
                        font = font,
                        tapPackage = theme.weatherTapPackage,
                        onTap = { pkg -> vm.launchByPackage(pkg) },
                    )
                }
                if (theme.showNowPlaying) {
                    NowPlayingLine(
                        ctx = ctx,
                        textAlign = composeTextAlign,
                        bodySp = bodySp,
                        font = font,
                        tapPackage = theme.musicTapPackage,
                        onTap = { pkg -> vm.launchByPackage(pkg) },
                    )
                }
                if (theme.showCalendar && calendarEvents.isNotEmpty()) {
                    CalendarBlock(
                        events = calendarEvents,
                        clock24h = theme.clock24h,
                        textAlign = composeTextAlign,
                        horizAlign = horizAlign,
                        bodySp = bodySp,
                        font = font,
                        tapPackage = theme.calendarTapPackage,
                        onTap = { pkg -> vm.launchByPackage(pkg) },
                    )
                }
            }

            // Gap between clock block and apps (not needed for spread — SpaceBetween handles it)
            if (theme.verticalLayout != "spread") {
                Spacer(Modifier.height(40.dp))
            }

            // Pinned apps
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = horizAlign,
            ) {
                pinnedApps.forEach { app ->
                    Text(
                        text = if (app.packageName in notifPackages) "${app.displayLabel} ·" else app.displayLabel,
                        fontSize = bodySp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = composeTextAlign,
                        fontFamily = font,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { vm.launchApp(app) },
                                onLongClick = { vm.navigate(Screen.AppSettings(app)) },
                            )
                            .padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherLine(
    state: WeatherState,
    textAlign: TextAlign,
    bodySp: TextUnit,
    font: androidx.compose.ui.text.font.FontFamily,
    tapPackage: String?,
    onTap: (String) -> Unit,
) {
    if (state is WeatherState.Ready) {
        val unit = if (state.isFahrenheit) "°F" else "°C"
        Text(
            text = "${state.temp}$unit  ${state.condition}",
            fontSize = bodySp * 0.83f,
            color = MaterialTheme.dimContent,
            textAlign = textAlign,
            fontFamily = font,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .then(if (tapPackage != null) Modifier.clickable { onTap(tapPackage) } else Modifier),
        )
    }
}

@Composable
private fun NowPlayingLine(
    ctx: android.content.Context,
    textAlign: TextAlign,
    bodySp: TextUnit,
    font: androidx.compose.ui.text.font.FontFamily,
    tapPackage: String?,
    onTap: (String) -> Unit,
) {
    val info by NowPlayingStore.nowPlaying.collectAsState()
    val isEnabled = remember { NowPlayingStore.isListenerEnabled(ctx) }
    if (isEnabled && info != null) {
        val nowInfo = info!!
        val displayText = if (nowInfo.artist.isNotBlank()) {
            "${nowInfo.title}  ·  ${nowInfo.artist}"
        } else {
            nowInfo.title
        }
        Text(
            text = displayText,
            fontSize = bodySp * 0.83f,
            color = MaterialTheme.dimContent,
            textAlign = textAlign,
            fontFamily = font,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .then(if (tapPackage != null) Modifier.clickable { onTap(tapPackage) } else Modifier),
        )
    }
}

@Composable
private fun CalendarBlock(
    events: List<CalendarEvent>,
    clock24h: Boolean,
    textAlign: TextAlign,
    horizAlign: Alignment.Horizontal,
    bodySp: TextUnit,
    font: androidx.compose.ui.text.font.FontFamily,
    tapPackage: String?,
    onTap: (String) -> Unit,
) {
    val clickMod = if (tapPackage != null) Modifier.clickable { onTap(tapPackage) } else Modifier
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .then(clickMod),
        horizontalAlignment = horizAlign,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        events.forEach { event ->
            val label = "${event.timeLabel(clock24h)}  ${event.title}"
            Text(
                text = label,
                fontSize = bodySp * 0.83f,
                color = MaterialTheme.dimContent,
                textAlign = textAlign,
                fontFamily = font,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun String.toClockSp(): TextUnit = when (this) {
    "small" -> 52.sp
    "large" -> 92.sp
    else -> 72.sp
}

private fun String.toBodySp(): TextUnit = when (this) {
    "small" -> 15.sp
    "large" -> 22.sp
    else -> 18.sp
}

private fun String.toColumnAlign(): Alignment.Horizontal = when (this) {
    "center" -> Alignment.CenterHorizontally
    "right" -> Alignment.End
    else -> Alignment.Start
}

private fun String.toTextAlign(): TextAlign = when (this) {
    "center" -> TextAlign.Center
    "right" -> TextAlign.End
    else -> TextAlign.Start
}

private fun String.toArrangement(): Arrangement.Vertical = when (this) {
    "top" -> Arrangement.Top
    "center" -> Arrangement.Center
    "bottom" -> Arrangement.Bottom
    else -> Arrangement.SpaceBetween
}

private fun currentTimeStr(clock24h: Boolean): String {
    val pattern = if (clock24h) "HH:mm" else "h:mm a"
    return LocalTime.now().format(DateTimeFormatter.ofPattern(pattern))
}

private fun currentDateStr(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d"))

private fun hasLocationPermission(ctx: Context): Boolean =
    ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private fun getLastLocation(ctx: Context): Pair<Double, Double>? {
    val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    )
        .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time }
        ?.let { it.latitude to it.longitude }
}

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

private fun expandNotifications(ctx: Context) {
    try {
        val sm = ctx.getSystemService("statusbar")
        val cls = Class.forName("android.app.StatusBarManager")
        cls.getMethod("expandNotificationsPanel").invoke(sm)
    } catch (_: Exception) {}
}
