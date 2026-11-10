package com.sparse.launcher

data class AppInfo(
    val packageName: String,
    val label: String,
    val customLabel: String? = null,
) {
    val displayLabel: String get() = customLabel?.takeIf { it.isNotBlank() } ?: label
}
