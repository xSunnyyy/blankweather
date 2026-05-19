package com.blankweather.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankweather.app.ui.WeatherScreen
import com.blankweather.app.ui.WeatherViewModel
import com.blankweather.app.ui.theme.BlankWeatherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlankWeatherTheme {
                val vm: WeatherViewModel = viewModel(
                    factory = WeatherViewModel.factory(applicationContext)
                )
                WeatherScreen(viewModel = vm)
            }
        }
    }
}
