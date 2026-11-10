package com.sparse.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sparse.launcher.ui.theme.dimContent
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(vm: LauncherViewModel) {
    val ctx = LocalContext.current
    val pinnedApps by vm.pinnedApps.collectAsState()
    val theme by vm.themeState.collectAsState()
    val weatherState by vm.weatherState.collectAsState()

    var time by remember { mutableStateOf(currentTimeStr(theme.clock24h)) }
    var date by remember { mutableStateOf(currentDateStr()) }

    LaunchedEffect(theme.clock24h) {
        while (true) {
            time = currentTimeStr(theme.clock24h)
            date = currentDateStr()
            delay(10_000)
        }
    }

    // Location permission + weather fetch
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) {
            getLastLocation(ctx)?.let { (lat, lon) -> vm.fetchWeather(lat, lon) }
                ?: vm.setWeatherError()
        } else {
            vm.setWeatherError()
        }
    }

    LaunchedEffect(weatherState) {
        if (weatherState !is WeatherState.Idle) return@LaunchedEffect
        val granted = hasLocationPermission(ctx)
        if (granted) {
            getLastLocation(ctx)?.let { (lat, lon) -> vm.fetchWeather(lat, lon) }
                ?: vm.setWeatherError()
        } else {
            permLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -40f) vm.navigate(Screen.Drawer)
                    else if (dragAmount > 40f) expandNotifications(ctx)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = time,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 76.sp,
                )
                Text(
                    text = date,
                    fontSize = 16.sp,
                    color = MaterialTheme.dimContent,
                    modifier = Modifier.padding(top = 4.dp),
                )
                WeatherLine(weatherState)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(pinnedApps, key = { it.packageName }) { app ->
                    Text(
                        text = app.displayLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground,
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
private fun WeatherLine(state: WeatherState) {
    when (state) {
        is WeatherState.Ready -> {
            val unit = if (state.isFahrenheit) "°F" else "°C"
            Text(
                text = "${state.temp}$unit  ${state.condition}",
                fontSize = 15.sp,
                color = MaterialTheme.dimContent,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        else -> {}
    }
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

private fun expandNotifications(ctx: Context) {
    try {
        val sm = ctx.getSystemService("statusbar")
        val cls = Class.forName("android.app.StatusBarManager")
        cls.getMethod("expandNotificationsPanel").invoke(sm)
    } catch (_: Exception) {}
}
