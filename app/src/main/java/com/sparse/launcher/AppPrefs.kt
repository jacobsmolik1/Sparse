package com.sparse.launcher

import android.content.Context
import android.content.SharedPreferences

class AppPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sparse_prefs", Context.MODE_PRIVATE)

    var pinnedApps: List<String>
        get() = prefs.getStringSet(KEY_PINNED, emptySet())!!.toList()
        set(value) = prefs.edit().putStringSet(KEY_PINNED, value.toSet()).apply()

    var mindfulApps: Set<String>
        get() = prefs.getStringSet(KEY_MINDFUL, emptySet())!!
        set(value) = prefs.edit().putStringSet(KEY_MINDFUL, value).apply()

    var focusModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_FOCUS, false)
        set(value) = prefs.edit().putBoolean(KEY_FOCUS, value).apply()

    var focusBlockedApps: Set<String>
        get() = prefs.getStringSet(KEY_FOCUS_BLOCKED, emptySet())!!
        set(value) = prefs.edit().putStringSet(KEY_FOCUS_BLOCKED, value).apply()

    fun getDailyLimit(packageName: String): Int =
        prefs.getInt("limit_$packageName", 0)

    fun setDailyLimit(packageName: String, minutes: Int) =
        prefs.edit().putInt("limit_$packageName", minutes).apply()

    fun getCustomLabel(packageName: String): String? =
        prefs.getString("label_$packageName", null)?.takeIf { it.isNotBlank() }

    fun setCustomLabel(packageName: String, label: String) =
        prefs.edit().putString("label_$packageName", label).apply()

    companion object {
        private const val KEY_PINNED = "pinned_apps"
        private const val KEY_MINDFUL = "mindful_apps"
        private const val KEY_FOCUS = "focus_mode"
        private const val KEY_FOCUS_BLOCKED = "focus_blocked"
    }
}
