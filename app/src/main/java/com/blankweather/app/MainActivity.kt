package com.blankweather.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankweather.app.ui.WeatherScreen
import com.blankweather.app.ui.WeatherViewModel
import com.blankweather.app.ui.theme.BlankWeatherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: WeatherViewModel = viewModel(
                factory = WeatherViewModel.factory(applicationContext)
            )
            val settings by vm.settings.collectAsState()
            BlankWeatherTheme(themeMode = settings.themeMode) {
                WeatherScreen(viewModel = vm)
            }
        }
    }
}
