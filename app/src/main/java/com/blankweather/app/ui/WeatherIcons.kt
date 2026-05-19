package com.blankweather.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blankweather.app.data.WeatherKind

@Composable
fun WeatherIcon(
    kind: WeatherKind,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidthDp: Dp = 1.6.dp,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round)
        when (kind) {
            WeatherKind.CLEAR -> drawSun(color, stroke)
            WeatherKind.MAINLY_CLEAR -> drawCloudWithSun(color, stroke)
            WeatherKind.CLOUDY, WeatherKind.FOG -> drawCloud(color, stroke)
            WeatherKind.DRIZZLE, WeatherKind.RAIN, WeatherKind.SHOWERS -> drawCloudRain(color, stroke)
            WeatherKind.SNOW -> drawCloudSnow(color, stroke)
            WeatherKind.THUNDERSTORM -> drawCloudBolt(color, stroke)
        }
    }
}

private fun DrawScope.drawCloud(color: Color, stroke: Stroke, cloudOffset: Offset = Offset.Zero) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        val cx = w * 0.5f + cloudOffset.x
        val cy = h * 0.55f + cloudOffset.y
        val r = h * 0.22f
        moveTo(cx - r * 1.8f, cy + r * 0.6f)
        cubicTo(
            cx - r * 2.0f, cy - r * 0.4f,
            cx - r * 1.2f, cy - r * 1.2f,
            cx - r * 0.4f, cy - r * 0.7f
        )
        cubicTo(
            cx - r * 0.1f, cy - r * 1.4f,
            cx + r * 0.9f, cy - r * 1.4f,
            cx + r * 1.1f, cy - r * 0.5f
        )
        cubicTo(
            cx + r * 2.1f, cy - r * 0.5f,
            cx + r * 2.1f, cy + r * 0.6f,
            cx + r * 1.4f, cy + r * 0.6f
        )
        lineTo(cx - r * 1.8f, cy + r * 0.6f)
        close()
    }
    drawPath(path, color = color, style = stroke)
}

private fun DrawScope.drawCloudRain(color: Color, stroke: Stroke) {
    drawCloud(color, stroke, Offset(0f, -size.height * 0.08f))
    val w = size.width
    val h = size.height
    val baseY = h * 0.72f
    val dy = h * 0.22f
    val dx = w * 0.10f
    val rainStroke = Stroke(width = stroke.width, cap = StrokeCap.Round)
    for (i in 0..2) {
        val x = w * (0.32f + i * 0.18f)
        drawLine(
            color = color,
            start = Offset(x + dx, baseY),
            end = Offset(x, baseY + dy),
            strokeWidth = rainStroke.width,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCloudSnow(color: Color, stroke: Stroke) {
    drawCloud(color, stroke, Offset(0f, -size.height * 0.08f))
    val w = size.width
    val h = size.height
    val y = h * 0.85f
    val r = h * 0.04f
    for (i in 0..2) {
        val x = w * (0.30f + i * 0.18f)
        drawCircle(color = color, radius = r, center = Offset(x, y), style = stroke)
    }
}

private fun DrawScope.drawCloudBolt(color: Color, stroke: Stroke) {
    drawCloud(color, stroke, Offset(0f, -size.height * 0.08f))
    val w = size.width
    val h = size.height
    val bolt = Path().apply {
        moveTo(w * 0.50f, h * 0.62f)
        lineTo(w * 0.40f, h * 0.85f)
        lineTo(w * 0.50f, h * 0.85f)
        lineTo(w * 0.44f, h * 0.98f)
    }
    drawPath(bolt, color = color, style = stroke)
}

private fun DrawScope.drawSun(color: Color, stroke: Stroke) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val r = size.minDimension * 0.22f
    drawCircle(color = color, radius = r, center = Offset(cx, cy), style = stroke)
    val rayInner = r * 1.5f
    val rayOuter = r * 2.1f
    for (i in 0 until 8) {
        val angle = (i * 45f) * (Math.PI / 180.0)
        val sin = kotlin.math.sin(angle).toFloat()
        val cos = kotlin.math.cos(angle).toFloat()
        drawLine(
            color = color,
            start = Offset(cx + cos * rayInner, cy + sin * rayInner),
            end = Offset(cx + cos * rayOuter, cy + sin * rayOuter),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCloudWithSun(color: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val sunCx = w * 0.32f
    val sunCy = h * 0.40f
    val r = h * 0.14f
    drawCircle(color = color, radius = r, center = Offset(sunCx, sunCy), style = stroke)
    val rays = 6
    val rayInner = r * 1.4f
    val rayOuter = r * 1.95f
    for (i in 0 until rays) {
        val angle = (-150f + i * 60f) * (Math.PI / 180.0)
        val sin = kotlin.math.sin(angle).toFloat()
        val cos = kotlin.math.cos(angle).toFloat()
        drawLine(
            color = color,
            start = Offset(sunCx + cos * rayInner, sunCy + sin * rayInner),
            end = Offset(sunCx + cos * rayOuter, sunCy + sin * rayOuter),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
    drawCloud(color, stroke, Offset(w * 0.10f, h * 0.06f))
}

