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

sealed class Screen {
    object Home : Screen()
    object Drawer : Screen()
    object Settings : Screen()
    object ManageHomeApps : Screen()
    data class Gate(val app: AppInfo) : Screen()
    data class AppSettings(val app: AppInfo) : Screen()
}

data class ThemeState(
    val bgColorIndex: Int,
    val textColorIndex: Int,
    val clock24h: Boolean,
)

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
        )
    )
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

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
        val isMindful = app.packageName in prefs.mindfulApps
        val isBlocked = prefs.focusModeEnabled && app.packageName in prefs.focusBlockedApps
        if (isMindful || isBlocked) _screen.value = Screen.Gate(app)
        else doLaunch(app)
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

    fun setCustomLabel(packageName: String, label: String) {
        prefs.setCustomLabel(packageName, label)
        loadApps()
    }

    fun setDailyLimit(packageName: String, minutes: Int) {
        prefs.setDailyLimit(packageName, minutes)
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

    fun filteredApps(): List<AppInfo> {
        val q = _drawerQuery.value.trim().lowercase()
        return if (q.isBlank()) _allApps.value
        else _allApps.value.filter { it.displayLabel.lowercase().contains(q) }
    }

    fun unpinnedApps(): List<AppInfo> {
        val pinned = prefs.pinnedApps.toSet()
        return _allApps.value.filter { it.packageName !in pinned }
    }
}
