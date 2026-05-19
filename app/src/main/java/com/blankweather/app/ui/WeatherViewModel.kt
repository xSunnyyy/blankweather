package com.blankweather.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blankweather.app.data.AppSettings
import com.blankweather.app.data.SettingsStore
import com.blankweather.app.data.TempUnit
import com.blankweather.app.data.ThemeMode
import com.blankweather.app.data.WeatherRepository
import com.blankweather.app.data.WeatherSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object NeedsPermission : WeatherUiState
    data class Loaded(val snapshot: WeatherSnapshot) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(
    private val repo: WeatherRepository,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsStore.state

    fun onPermissionResolved(granted: Boolean) {
        if (granted) refresh() else _state.value = WeatherUiState.NeedsPermission
    }

    fun ensurePermissionThenLoad() {
        if (repo.hasLocationPermission()) {
            if (_state.value is WeatherUiState.Loaded) return
            refresh()
        } else {
            _state.value = WeatherUiState.NeedsPermission
        }
    }

    fun refresh() {
        if (_refreshing.value) return
        _refreshing.update { true }
        viewModelScope.launch {
            try {
                val snapshot = repo.load(settingsStore.state.value.tempUnit)
                _state.value = WeatherUiState.Loaded(snapshot)
            } catch (t: Throwable) {
                _state.value = WeatherUiState.Error(t.message ?: "Something went wrong.")
            } finally {
                _refreshing.update { false }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        settingsStore.setTheme(mode)
    }

    fun setUnit(unit: TempUnit) {
        if (settingsStore.state.value.tempUnit == unit) return
        settingsStore.setUnit(unit)
        refresh()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = context.applicationContext
                WeatherViewModel(
                    repo = WeatherRepository(app),
                    settingsStore = SettingsStore(app),
                )
            }
        }
    }
}
