package com.sparse.launcher

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

class UsageStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sparse_usage", Context.MODE_PRIVATE)

    private fun todayKey(packageName: String): String =
        "opens_${LocalDate.now()}_$packageName"

    fun recordOpen(packageName: String) {
        val key = todayKey(packageName)
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun getOpensToday(packageName: String): Int =
        prefs.getInt(todayKey(packageName), 0)

    fun clearOldEntries() {
        val today = LocalDate.now().toString()
        prefs.all.keys
            .filter { it.startsWith("opens_") && !it.startsWith("opens_$today") }
            .forEach { prefs.edit().remove(it).apply() }
    }
}
