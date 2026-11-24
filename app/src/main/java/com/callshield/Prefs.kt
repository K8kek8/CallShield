package com.callshield

import android.content.Context

/** Estado compartido entre la pantalla y el servicio que filtra las llamadas. */
object Prefs {
    private const val FILE = "callshield"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_PAUSED_UNTIL = "paused_until"
    private const val KEY_BLOCKED = "blocked_count"

    private fun sp(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isEnabled(c: Context) = sp(c).getBoolean(KEY_ENABLED, false)
    fun setEnabled(c: Context, value: Boolean) {
        sp(c).edit().putBoolean(KEY_ENABLED, value).apply()
        if (!value) clearPause(c)
    }

    fun pausedUntil(c: Context) = sp(c).getLong(KEY_PAUSED_UNTIL, 0L)
    fun pauseFor(c: Context, millis: Long) {
        sp(c).edit().putLong(KEY_PAUSED_UNTIL, System.currentTimeMillis() + millis).apply()
    }
    fun clearPause(c: Context) {
        sp(c).edit().putLong(KEY_PAUSED_UNTIL, 0L).apply()
    }
    fun isPaused(c: Context) = System.currentTimeMillis() < pausedUntil(c)

    /** El filtro bloquea solo si está encendido y no hay una pausa en curso. */
    fun isActive(c: Context) = isEnabled(c) && !isPaused(c)

    fun blockedCount(c: Context) = sp(c).getInt(KEY_BLOCKED, 0)
    fun incrementBlocked(c: Context) {
        sp(c).edit().putInt(KEY_BLOCKED, blockedCount(c) + 1).apply()
    }
}
