package com.blankweather.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class TempUnit(val apiValue: String, val symbol: String) {
    CELSIUS("celsius", "C"),
    FAHRENHEIT("fahrenheit", "F"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
)

class SettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("blank_weather", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    private fun load(): AppSettings = AppSettings(
        themeMode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)!!)
        }.getOrDefault(ThemeMode.SYSTEM),
        tempUnit = runCatching {
            TempUnit.valueOf(prefs.getString(KEY_UNIT, TempUnit.CELSIUS.name)!!)
        }.getOrDefault(TempUnit.CELSIUS),
    )

    fun setTheme(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME, mode.name) }
        _state.value = _state.value.copy(themeMode = mode)
    }

    fun setUnit(unit: TempUnit) {
        prefs.edit { putString(KEY_UNIT, unit.name) }
        _state.value = _state.value.copy(tempUnit = unit)
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_UNIT = "temp_unit"
    }
}
