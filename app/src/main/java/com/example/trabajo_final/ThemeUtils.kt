package com.example.trabajo_final

import android.content.Context
import android.widget.ScrollView

object ThemeUtils {
    fun saveThemeState(context: Context, isDarkTheme: Boolean) {
        val sharedPreferences = context.getSharedPreferences("ThemePref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDarkTheme", isDarkTheme)
        editor.apply()
    }

    fun loadThemeState(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("ThemePref", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isDarkTheme", false)
    }
}