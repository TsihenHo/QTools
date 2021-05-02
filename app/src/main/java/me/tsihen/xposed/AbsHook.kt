package me.tsihen.xposed

import me.tsihen.config.ConfigManager.Companion.config

abstract class AbsHook {
    private var inited: Boolean = false
    fun init(): Boolean {
        if (inited) return true
        if (!isEnabled()) return true
        return doInit()
    }

    protected abstract fun doInit(): Boolean
    open fun isEnabled(): Boolean {
        return config.getOr(getId(), false)
    }

    open fun setEnabled(z: Boolean) {
        config[getId()] = z
    }

    fun getId(): String {
        return "enableHook-" + this.javaClass.simpleName
    }
}