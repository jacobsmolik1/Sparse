package com.sparse.launcher

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationStore {
    private val _active = MutableStateFlow<Set<String>>(emptySet())
    val active: StateFlow<Set<String>> = _active.asStateFlow()

    fun post(pkg: String) {
        _active.value = _active.value + pkg
    }

    fun remove(pkg: String) {
        _active.value = _active.value - pkg
    }

    fun clear() {
        _active.value = emptySet()
    }
}
