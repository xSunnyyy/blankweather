package com.blankweather.app.ui

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blankweather.app.data.AppSettings
import com.blankweather.app.data.ForecastResponse
import com.blankweather.app.data.TempUnit
import com.blankweather.app.data.ThemeMode
import com.blankweather.app.data.WeatherCode
import com.blankweather.app.data.WeatherSnapshot
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    LaunchedEffect(permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted ||
            permissions.permissions.any { it.status.isGranted }
        ) {
            viewModel.onPermissionResolved(true)
        } else {
            viewModel.ensurePermissionThenLoad()
        }
    }

    val state by viewModel.state.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showSettings by remember { mutableStateOf(false) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            when (val s = state) {
                WeatherUiState.Idle -> CenteredHint("Loading…")
                WeatherUiState.NeedsPermission -> PermissionPrompt(
                    onRequest = { permissions.launchMultiplePermissionRequest() }
                )
                is WeatherUiState.Error -> ErrorView(
                    message = s.message,
                    onRetry = { viewModel.refresh() }
                )
                is WeatherUiState.Loaded -> WeatherContent(
                    snapshot = s.snapshot,
                    onOpenSettings = { showSettings = true },
                )
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            settings = settings,
            onDismiss = { showSettings = false },
            onThemeChange = viewModel::setThemeMode,
            onUnitChange = viewModel::setUnit,
        )
    }
}

@Composable
private fun WeatherContent(
    snapshot: WeatherSnapshot,
    onOpenSettings: () -> Unit,
) {
    val forecast = snapshot.forecast
    val now = forecast.current
    val tempInt = now.temperature.roundToInt()
    val cityLabel = (snapshot.city ?: "Current Location").uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = cityLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(36.dp))
        Text(
            text = "$tempInt°",
            fontSize = 112.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = WeatherCode.describe(now.weatherCode),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(36.dp))
        HourlyRow(forecast)

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))

        DailyList(forecast)

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenSettings)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "…",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Time",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = formatNow(now.time),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HourlyRow(forecast: ForecastResponse) {
    val nowIndex = currentHourIndex(forecast)
    val items = (0..5).mapNotNull { step ->
        val i = nowIndex + step * 3
        if (i in forecast.hourly.time.indices) {
            HourItem(
                hourLabel = formatHour(forecast.hourly.time[i]),
                code = forecast.hourly.weatherCode[i],
                probability = forecast.hourly.precipitationProbability.getOrNull(i) ?: 0,
            )
        } else null
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        items.forEach { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.hourLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                WeatherIcon(
                    kind = WeatherCode.kind(item.code),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(36.dp),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${item.probability}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "%",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp, start = 1.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyList(forecast: ForecastResponse) {
    val days = forecast.daily
    val count = minOf(7, days.time.size)
    for (i in 0 until count) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (i == 0) "Today" else formatDay(days.time[i]),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            WeatherIcon(
                kind = WeatherCode.kind(days.weatherCode[i]),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${days.temperatureMin[i].roundToInt()}° / ${days.temperatureMax[i].roundToInt()}°",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onUnitChange: (TempUnit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 8.dp),
        ) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center,
            )

            SettingRow(label = "Theme") {
                val options = ThemeMode.entries
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { idx, mode ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(idx, options.size),
                        ) { Text(mode.label()) }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SettingRow(label = "Units") {
                val options = TempUnit.entries
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { idx, unit ->
                        SegmentedButton(
                            selected = settings.tempUnit == unit,
                            onClick = { onUnitChange(unit) },
                            shape = SegmentedButtonDefaults.itemShape(idx, options.size),
                        ) { Text("°${unit.symbol}") }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingRow(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

@Composable
private fun PermissionPrompt(onRequest: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "LOCATION",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Blank Weather uses your location to fetch the forecast from Open-Meteo. Nothing is stored or shared.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp),
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRequest,
                shape = RoundedCornerShape(2.dp),
            ) {
                Text("Allow location")
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRetry, shape = RoundedCornerShape(2.dp)) {
            Text("Retry")
        }
    }
}

@Composable
private fun CenteredHint(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private data class HourItem(val hourLabel: String, val code: Int, val probability: Int)

private fun currentHourIndex(forecast: ForecastResponse): Int {
    val nowTime = forecast.current.time
    val matchByExact = forecast.hourly.time.indexOf(
        nowTime.substring(0, minOf(13, nowTime.length)) + ":00"
    )
    if (matchByExact >= 0) return matchByExact
    val nowDate = LocalDateTime.parse(nowTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val truncated = nowDate.withMinute(0).withSecond(0).withNano(0)
    val idx = forecast.hourly.time.indexOfFirst { entry ->
        runCatching {
            LocalDateTime.parse(entry, DateTimeFormatter.ISO_LOCAL_DATE_TIME) >= truncated
        }.getOrDefault(false)
    }
    return if (idx >= 0) idx else 0
}

private fun formatHour(iso: String): String {
    val dt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val hour = dt.hour
    val display = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return display.toString()
}

private fun formatDay(iso: String): String {
    val date = java.time.LocalDate.parse(iso)
    return date.format(DateTimeFormatter.ofPattern("EEE"))
}

private fun formatNow(iso: String): String {
    val dt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    return dt.format(DateTimeFormatter.ofPattern("EEE, MMM d h:mm a"))
}
