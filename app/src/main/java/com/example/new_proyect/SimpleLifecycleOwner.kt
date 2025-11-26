package com.example.new_proyect

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Proporciona un LifecycleOwner simple para contextos que no lo tienen por defecto
 * (como InputMethodService o Custom View)
 */
class SimpleLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle = lifecycleRegistry

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }
}

