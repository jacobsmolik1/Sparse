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

    // "small" | "medium" | "large"
    var clockSize: String
        get() = prefs.getString(KEY_CLOCK_SIZE, "medium") ?: "medium"
        set(value) = prefs.edit().putString(KEY_CLOCK_SIZE, value).apply()

    // "small" | "medium" | "large"
    var bodySize: String
        get() = prefs.getString(KEY_BODY_SIZE, "medium") ?: "medium"
        set(value) = prefs.edit().putString(KEY_BODY_SIZE, value).apply()

    var bgColorIndex: Int
        get() = prefs.getInt(KEY_BG_COLOR, 0)
        set(value) = prefs.edit().putInt(KEY_BG_COLOR, value).apply()

    var textColorIndex: Int
        get() = prefs.getInt(KEY_TEXT_COLOR, 0)
        set(value) = prefs.edit().putInt(KEY_TEXT_COLOR, value).apply()

    var hiddenApps: Set<String>
        get() = prefs.getStringSet(KEY_HIDDEN, emptySet())!!
        set(value) = prefs.edit().putStringSet(KEY_HIDDEN, value).apply()

    var swipeLeftPackage: String?
        get() = prefs.getString(KEY_SWIPE_LEFT, null)
        set(value) = if (value == null) prefs.edit().remove(KEY_SWIPE_LEFT).apply()
                     else prefs.edit().putString(KEY_SWIPE_LEFT, value).apply()

    var swipeRightPackage: String?
        get() = prefs.getString(KEY_SWIPE_RIGHT, null)
        set(value) = if (value == null) prefs.edit().remove(KEY_SWIPE_RIGHT).apply()
                     else prefs.edit().putString(KEY_SWIPE_RIGHT, value).apply()

    var showClock: Boolean
        get() = prefs.getBoolean(KEY_SHOW_CLOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_CLOCK, value).apply()

    var showDate: Boolean
        get() = prefs.getBoolean(KEY_SHOW_DATE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_DATE, value).apply()

    var showWeather: Boolean
        get() = prefs.getBoolean(KEY_SHOW_WEATHER, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_WEATHER, value).apply()

    var showNowPlaying: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NOW_PLAYING, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NOW_PLAYING, value).apply()

    var fontFamily: String
        get() = prefs.getString(KEY_FONT_FAMILY, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_FONT_FAMILY, value).apply()

    var clockTapPackage: String?
        get() = prefs.getString(KEY_CLOCK_TAP, null)
        set(value) = if (value == null) prefs.edit().remove(KEY_CLOCK_TAP).apply()
                     else prefs.edit().putString(KEY_CLOCK_TAP, value).apply()

    var weatherTapPackage: String?
        get() = prefs.getString(KEY_WEATHER_TAP, null)
        set(value) = if (value == null) prefs.edit().remove(KEY_WEATHER_TAP).apply()
                     else prefs.edit().putString(KEY_WEATHER_TAP, value).apply()

    var musicTapPackage: String?
        get() = prefs.getString(KEY_MUSIC_TAP, null)
        set(value) = if (value == null) prefs.edit().remove(KEY_MUSIC_TAP).apply()
                     else prefs.edit().putString(KEY_MUSIC_TAP, value).apply()

    var showCalendar: Boolean
        get() = prefs.getBoolean(KEY_SHOW_CALENDAR, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_CALENDAR, value).apply()

    var calendarTapPackage: String?
        get() = prefs.getString(KEY_CALENDAR_TAP, null)
        set(value) = if (value == null) prefs.edit().remove(KEY_CALENDAR_TAP).apply()
                     else prefs.edit().putString(KEY_CALENDAR_TAP, value).apply()

    fun getDailyLimit(packageName: String): Int =
        prefs.getInt("limit_$packageName", 0)

    fun setDailyLimit(packageName: String, opens: Int) =
        prefs.edit().putInt("limit_$packageName", opens).apply()

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
        private const val KEY_CLOCK_SIZE = "clock_size"
        private const val KEY_BODY_SIZE = "body_size"
        private const val KEY_HIDDEN = "hidden_apps"
        private const val KEY_SWIPE_LEFT = "swipe_left_package"
        private const val KEY_SWIPE_RIGHT = "swipe_right_package"
        private const val KEY_SHOW_CLOCK = "show_clock"
        private const val KEY_SHOW_DATE = "show_date"
        private const val KEY_SHOW_WEATHER = "show_weather"
        private const val KEY_SHOW_NOW_PLAYING = "show_now_playing"
        private const val KEY_FONT_FAMILY = "font_family"
        private const val KEY_CLOCK_TAP = "clock_tap_package"
        private const val KEY_WEATHER_TAP = "weather_tap_package"
        private const val KEY_MUSIC_TAP = "music_tap_package"
        private const val KEY_SHOW_CALENDAR = "show_calendar"
        private const val KEY_CALENDAR_TAP = "calendar_tap_package"
    }
}
