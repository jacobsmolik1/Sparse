package com.sparse.launcher

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sparse.launcher.ui.theme.BG_COLORS
import com.sparse.launcher.ui.theme.TEXT_COLORS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GateReason { Mindful, LimitReached, FocusBlock }

sealed class Screen {
    object Home : Screen()
    object Drawer : Screen()
    object Settings : Screen()
    object ManageHomeApps : Screen()
    object HiddenApps : Screen()
    data class Gate(val app: AppInfo, val reason: GateReason = GateReason.Mindful) : Screen()
    data class AppSettings(val app: AppInfo) : Screen()
    data class PickSwipeApp(val direction: String) : Screen()
    data class PickApp(val target: String) : Screen()
}

data class ThemeState(
    val bgColorIndex: Int,
    val textColorIndex: Int,
    val clock24h: Boolean,
    val useFahrenheit: Boolean,
    val textAlign: String,
    val verticalLayout: String,
    val clockSize: String,
    val bodySize: String,
    val showClock: Boolean,
    val showDate: Boolean,
    val showWeather: Boolean,
    val showNowPlaying: Boolean,
    val fontFamily: String,
    val swipeLeftPackage: String?,
    val swipeRightPackage: String?,
    val clockTapPackage: String?,
    val weatherTapPackage: String?,
    val musicTapPackage: String?,
    val showCalendar: Boolean,
    val calendarTapPackage: String?,
    val hideStatusBar: Boolean,
)

sealed class WeatherState {
    object Idle : WeatherState()
    object Loading : WeatherState()
    data class Ready(val temp: Int, val condition: String, val isFahrenheit: Boolean) : WeatherState()
    object Error : WeatherState()
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = AppPrefs(application)
    val usage = UsageStore(application)

    private val _screen = MutableStateFlow<Screen>(Screen.Home)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _pinnedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val pinnedApps: StateFlow<List<AppInfo>> = _pinnedApps.asStateFlow()

    private val _drawerQuery = MutableStateFlow("")
    val drawerQuery: StateFlow<String> = _drawerQuery.asStateFlow()

    private val _themeState = MutableStateFlow(
        ThemeState(
            bgColorIndex = prefs.bgColorIndex,
            textColorIndex = prefs.textColorIndex,
            clock24h = prefs.clock24h,
            useFahrenheit = prefs.useFahrenheit,
            textAlign = prefs.textAlign,
            verticalLayout = prefs.verticalLayout,
            clockSize = prefs.clockSize,
            bodySize = prefs.bodySize,
            showClock = prefs.showClock,
            showDate = prefs.showDate,
            showWeather = prefs.showWeather,
            showNowPlaying = prefs.showNowPlaying,
            fontFamily = prefs.fontFamily,
            swipeLeftPackage = prefs.swipeLeftPackage,
            swipeRightPackage = prefs.swipeRightPackage,
            clockTapPackage = prefs.clockTapPackage,
            weatherTapPackage = prefs.weatherTapPackage,
            musicTapPackage = prefs.musicTapPackage,
            showCalendar = prefs.showCalendar,
            calendarTapPackage = prefs.calendarTapPackage,
            hideStatusBar = prefs.hideStatusBar,
        )
    )
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val _calendarEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents.asStateFlow()

    private var lastLat = Double.NaN
    private var lastLon = Double.NaN
    var weatherPermissionDenied = false

    init {
        loadApps()
        usage.clearOldEntries()
    }

    fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val flags = PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            val resolved: List<ResolveInfo> = pm.queryIntentActivities(intent, flags)

            val apps = resolved
                .mapNotNull { ri ->
                    val pkg = ri.activityInfo.packageName
                    val label = ri.loadLabel(pm).toString()
                    val custom = prefs.getCustomLabel(pkg)
                    AppInfo(pkg, label, custom)
                }
                .sortedBy { it.displayLabel.lowercase() }

            _allApps.value = apps
            refreshPinned(apps)
        }
    }

    private fun refreshPinned(apps: List<AppInfo> = _allApps.value) {
        val pkgMap = apps.associateBy { it.packageName }
        _pinnedApps.value = prefs.pinnedApps.mapNotNull { pkgMap[it] }
    }

    fun navigate(screen: Screen) { _screen.value = screen }

    fun back() { _screen.value = Screen.Home }

    fun setDrawerQuery(q: String) { _drawerQuery.value = q }

    fun launchApp(app: AppInfo) {
        val isBlocked = prefs.focusModeEnabled && app.packageName in prefs.focusBlockedApps
        if (isBlocked) {
            _screen.value = Screen.Gate(app, GateReason.FocusBlock)
            return
        }
        val limit = prefs.getDailyLimit(app.packageName)
        if (limit > 0 && usage.getOpensToday(app.packageName) >= limit) {
            _screen.value = Screen.Gate(app, GateReason.LimitReached)
            return
        }
        val isMindful = app.packageName in prefs.mindfulApps
        if (isMindful) {
            _screen.value = Screen.Gate(app, GateReason.Mindful)
            return
        }
        doLaunch(app)
    }

    fun doLaunch(app: AppInfo) {
        val ctx = getApplication<Application>()
        usage.recordOpen(app.packageName)
        val intent = ctx.packageManager.getLaunchIntentForPackage(app.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent != null) ctx.startActivity(intent)
        _screen.value = Screen.Home
    }

    fun pinApp(packageName: String) {
        val current = prefs.pinnedApps.toMutableList()
        if (packageName !in current) {
            current.add(packageName)
            prefs.pinnedApps = current
            refreshPinned()
        }
    }

    fun unpinApp(packageName: String) {
        prefs.pinnedApps = prefs.pinnedApps.filter { it != packageName }
        refreshPinned()
    }

    fun movePinnedUp(packageName: String) {
        val list = prefs.pinnedApps.toMutableList()
        val idx = list.indexOf(packageName)
        if (idx > 0) {
            list.removeAt(idx)
            list.add(idx - 1, packageName)
            prefs.pinnedApps = list
            refreshPinned()
        }
    }

    fun movePinnedDown(packageName: String) {
        val list = prefs.pinnedApps.toMutableList()
        val idx = list.indexOf(packageName)
        if (idx in 0 until list.size - 1) {
            list.removeAt(idx)
            list.add(idx + 1, packageName)
            prefs.pinnedApps = list
            refreshPinned()
        }
    }

    fun toggleMindful(packageName: String) {
        val current = prefs.mindfulApps.toMutableSet()
        if (packageName in current) current.remove(packageName) else current.add(packageName)
        prefs.mindfulApps = current
    }

    fun toggleFocusBlocked(packageName: String) {
        val current = prefs.focusBlockedApps.toMutableSet()
        if (packageName in current) current.remove(packageName) else current.add(packageName)
        prefs.focusBlockedApps = current
    }

    fun hideApp(pkg: String) {
        val current = prefs.hiddenApps.toMutableSet()
        current.add(pkg)
        prefs.hiddenApps = current
    }

    fun unhideApp(pkg: String) {
        val current = prefs.hiddenApps.toMutableSet()
        current.remove(pkg)
        prefs.hiddenApps = current
    }

    fun setSwipeApp(direction: String, pkg: String?) =
        setTapApp(if (direction == "left") "swipeLeft" else "swipeRight", pkg)

    fun setTapApp(target: String, pkg: String?) {
        when (target) {
            "swipeLeft" -> { prefs.swipeLeftPackage = pkg; _themeState.value = _themeState.value.copy(swipeLeftPackage = pkg) }
            "swipeRight" -> { prefs.swipeRightPackage = pkg; _themeState.value = _themeState.value.copy(swipeRightPackage = pkg) }
            "clockTap" -> { prefs.clockTapPackage = pkg; _themeState.value = _themeState.value.copy(clockTapPackage = pkg) }
            "weatherTap" -> { prefs.weatherTapPackage = pkg; _themeState.value = _themeState.value.copy(weatherTapPackage = pkg) }
            "musicTap" -> { prefs.musicTapPackage = pkg; _themeState.value = _themeState.value.copy(musicTapPackage = pkg) }
            "calendarTap" -> { prefs.calendarTapPackage = pkg; _themeState.value = _themeState.value.copy(calendarTapPackage = pkg) }
        }
    }

    fun setHideStatusBar(v: Boolean) {
        prefs.hideStatusBar = v
        _themeState.value = _themeState.value.copy(hideStatusBar = v)
    }

    fun setShowCalendar(v: Boolean) {
        prefs.showCalendar = v
        _themeState.value = _themeState.value.copy(showCalendar = v)
    }

    fun refreshCalendar(ctx: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _calendarEvents.value = CalendarRepository.upcoming(ctx)
        }
    }

    fun launchByPackage(pkg: String) {
        val app = _allApps.value.find { it.packageName == pkg } ?: return
        launchApp(app)
    }

    fun setCustomLabel(packageName: String, label: String) {
        prefs.setCustomLabel(packageName, label)
        loadApps()
    }

    fun setDailyLimit(packageName: String, opens: Int) {
        prefs.setDailyLimit(packageName, opens)
    }

    fun setBgColor(index: Int) {
        prefs.bgColorIndex = index
        _themeState.value = _themeState.value.copy(bgColorIndex = index)
    }

    fun setTextColor(index: Int) {
        prefs.textColorIndex = index
        _themeState.value = _themeState.value.copy(textColorIndex = index)
    }

    fun setClock24h(value: Boolean) {
        prefs.clock24h = value
        _themeState.value = _themeState.value.copy(clock24h = value)
    }

    fun setUseFahrenheit(value: Boolean) {
        prefs.useFahrenheit = value
        WeatherRepository.invalidate()
        _themeState.value = _themeState.value.copy(useFahrenheit = value)
        _weatherState.value = WeatherState.Idle
    }

    fun setTextAlign(value: String) {
        prefs.textAlign = value
        _themeState.value = _themeState.value.copy(textAlign = value)
    }

    fun setVerticalLayout(value: String) {
        prefs.verticalLayout = value
        _themeState.value = _themeState.value.copy(verticalLayout = value)
    }

    fun setClockSize(value: String) {
        prefs.clockSize = value
        _themeState.value = _themeState.value.copy(clockSize = value)
    }

    fun setBodySize(value: String) {
        prefs.bodySize = value
        _themeState.value = _themeState.value.copy(bodySize = value)
    }

    fun setShowClock(v: Boolean) {
        prefs.showClock = v
        _themeState.value = _themeState.value.copy(showClock = v)
    }

    fun setShowDate(v: Boolean) {
        prefs.showDate = v
        _themeState.value = _themeState.value.copy(showDate = v)
    }

    fun setShowWeather(v: Boolean) {
        prefs.showWeather = v
        _themeState.value = _themeState.value.copy(showWeather = v)
    }

    fun setShowNowPlaying(v: Boolean) {
        prefs.showNowPlaying = v
        _themeState.value = _themeState.value.copy(showNowPlaying = v)
    }

    fun setFontFamily(v: String) {
        prefs.fontFamily = v
        _themeState.value = _themeState.value.copy(fontFamily = v)
    }

    fun updateLocation(lat: Double, lon: Double) {
        lastLat = lat
        lastLon = lon
    }

    fun fetchWeather() {
        if (lastLat.isNaN() || _weatherState.value is WeatherState.Loading) return
        _weatherState.value = WeatherState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = WeatherRepository.fetch(lastLat, lastLon, prefs.useFahrenheit)
                _weatherState.value = WeatherState.Ready(data.temp, data.condition, data.isFahrenheit)
            } catch (_: Exception) {
                _weatherState.value = WeatherState.Error
            }
        }
    }

    fun refreshWeather() {
        if (lastLat.isNaN()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = WeatherRepository.fetch(lastLat, lastLon, prefs.useFahrenheit)
                _weatherState.value = WeatherState.Ready(data.temp, data.condition, data.isFahrenheit)
            } catch (_: Exception) { /* keep existing Ready state on transient error */ }
        }
    }

    fun setWeatherError() {
        weatherPermissionDenied = true
        _weatherState.value = WeatherState.Error
    }

    fun filteredApps(): List<AppInfo> {
        val q = _drawerQuery.value.trim().lowercase()
        val hidden = prefs.hiddenApps
        return if (q.isBlank()) {
            _allApps.value
                .filter { it.packageName !in hidden }
                .sortedWith(compareByDescending<AppInfo> { usage.getOpensToday(it.packageName) }
                    .thenBy { it.displayLabel.lowercase() })
        } else {
            _allApps.value.filter { it.packageName !in hidden && it.displayLabel.lowercase().contains(q) }
        }
    }

    fun unpinnedApps(): List<AppInfo> {
        val pinned = prefs.pinnedApps.toSet()
        val hidden = prefs.hiddenApps
        return _allApps.value.filter { it.packageName !in pinned && it.packageName !in hidden }
    }
}
