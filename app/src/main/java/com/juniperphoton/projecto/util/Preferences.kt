package com.juniperphoton.projecto.util

import android.content.Context
import android.content.SharedPreferences

object Preferences {
    private const val PREFERENCE_NAME = "project_o"

    const val KEY_PICK = "pick"
    const val KEY_SHADOW = "shadow"
    const val KEY_FRAME = "frame"

    fun getSharedPreferences(context: Context?): SharedPreferences? {
        return getSharedPreferences(context, PREFERENCE_NAME)
    }

    private fun getSharedPreferences(context: Context?, name: String): SharedPreferences? {
        return context?.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    fun contains(context: Context, key: String): Boolean {
        return getSharedPreferences(context)?.contains(key) ?: false
    }

    fun setBoolean(context: Context?, key: String, value: Boolean) {
        getSharedPreferences(context)?.edit()?.putBoolean(key, value)?.commit()
    }

    fun getBoolean(context: Context?, key: String, def: Boolean): Boolean {
        return getSharedPreferences(context)?.getBoolean(key, def) ?: def
    }

    fun setInt(context: Context?, key: String, value: Int) {
        getSharedPreferences(context)?.edit()?.putInt(key, value)
    }

    fun getInt(context: Context?, key: String, def: Int): Int {
        return getSharedPreferences(context)?.getInt(key, def) ?: def
    }
}