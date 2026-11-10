package com.sparse.launcher

import android.content.Context
import android.content.SharedPreferences

class AppPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sparse_prefs", Context.MODE_PRIVATE)

    // Stored as pipe-delimited string to preserve order
    var pinnedApps: List<String>
        get() = prefs.getString(KEY_PINNED, "")
            ?.split("|")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        set(value) = prefs.edit().putString(KEY_PINNED, value.joinToString("|")).apply()

    var mindfulApps: Set<String>
        get() = prefs.getStringSet(KEY_MINDFUL, emptySet())!!
        set(value) = prefs.edit().putStringSet(KEY_MINDFUL, value).apply()

    var focusModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_FOCUS, false)
        set(value) = prefs.edit().putBoolean(KEY_FOCUS, value).apply()

    var focusBlockedApps: Set<String>
        get() = prefs.getStringSet(KEY_FOCUS_BLOCKED, emptySet())!!
        set(value) = prefs.edit().putStringSet(KEY_FOCUS_BLOCKED, value).apply()

    var clock24h: Boolean
        get() = prefs.getBoolean(KEY_CLOCK_24H, true)
        set(value) = prefs.edit().putBoolean(KEY_CLOCK_24H, value).apply()

    var useFahrenheit: Boolean
        get() = prefs.getBoolean(KEY_TEMP_UNIT, true)
        set(value) = prefs.edit().putBoolean(KEY_TEMP_UNIT, value).apply()

    // "left" | "center" | "right"
    var textAlign: String
        get() = prefs.getString(KEY_TEXT_ALIGN, "left") ?: "left"
        set(value) = prefs.edit().putString(KEY_TEXT_ALIGN, value).apply()

    // "spread" | "top" | "center" | "bottom"
    var verticalLayout: String
        get() = prefs.getString(KEY_VERTICAL_LAYOUT, "spread") ?: "spread"
        set(value) = prefs.edit().putString(KEY_VERTICAL_LAYOUT, value).apply()

    var bgColorIndex: Int
        get() = prefs.getInt(KEY_BG_COLOR, 0)
        set(value) = prefs.edit().putInt(KEY_BG_COLOR, value).apply()

    var textColorIndex: Int
        get() = prefs.getInt(KEY_TEXT_COLOR, 0)
        set(value) = prefs.edit().putInt(KEY_TEXT_COLOR, value).apply()

    fun getDailyLimit(packageName: String): Int =
        prefs.getInt("limit_$packageName", 0)

    fun setDailyLimit(packageName: String, minutes: Int) =
        prefs.edit().putInt("limit_$packageName", minutes).apply()

    fun getCustomLabel(packageName: String): String? =
        prefs.getString("label_$packageName", null)?.takeIf { it.isNotBlank() }

    fun setCustomLabel(packageName: String, label: String) =
        prefs.edit().putString("label_$packageName", label).apply()

    companion object {
        private const val KEY_PINNED = "pinned_apps_ordered"
        private const val KEY_MINDFUL = "mindful_apps"
        private const val KEY_FOCUS = "focus_mode"
        private const val KEY_FOCUS_BLOCKED = "focus_blocked"
        private const val KEY_CLOCK_24H = "clock_24h"
        private const val KEY_BG_COLOR = "bg_color_index"
        private const val KEY_TEXT_COLOR = "text_color_index"
        private const val KEY_TEMP_UNIT = "temp_fahrenheit"
        private const val KEY_TEXT_ALIGN = "text_align"
        private const val KEY_VERTICAL_LAYOUT = "vertical_layout"
    }
}
