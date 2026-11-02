package com.freeturn.app.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Зимний вариант сезонного героя: по нажатию вместо листвы разлетается снег - просто
 * белые точки, без звёздочек. Ореол и вращение фигуры общие с осенью, сезон меняет
 * только то, что летит. Осень выключается своим флагом, зима - этим.
 */
internal const val WINTER_VIBE = true

private const val FLAKE_COUNT = 16
private const val SNOW_MS = 1400

private const val SNOW_FROM_DEG = -165f
private const val SNOW_TO_DEG = -15f

@Immutable
private class Flake(
    val angleRad: Float,
    val speed: Float,
    val sizeDp: Float,
    val sway: Float,
    val phase: Float,
    val delay: Float,
)

private fun snowBatch(seed: Int): List<Flake> {
    val rnd = Random(seed)
    val sector = (SNOW_TO_DEG - SNOW_FROM_DEG) / FLAKE_COUNT
    return List(FLAKE_COUNT) { i ->
        val deg = SNOW_FROM_DEG + sector * (i + 0.1f + rnd.nextFloat() * 0.8f)
        Flake(
            angleRad = Math.toRadians(deg.toDouble()).toFloat(),
            speed = 0.6f + rnd.nextFloat() * 0.8f,
            sizeDp = 2f + rnd.nextFloat() * 2.5f,
            sway = 0.5f + rnd.nextFloat() * 0.5f,
            phase = rnd.nextFloat() * 2f * PI.toFloat(),
            delay = rnd.nextFloat() * 0.2f,
        )
    }
}

/**
 * Разлёт снега на каждую смену [burstKey]. Кладётся поверх героя, как и листва, и так же
 * рисует за своими границами. Снег легче листа: вылетает невысоко, а падает медленно
 * и с покачиванием.
 */
@Composable
internal fun SnowBurst(
    burstKey: Int,
    buttonSize: Dp,
    modifier: Modifier = Modifier
) {
    // На светлом фоне белая точка пропадает - подкладываем ей еле заметную тень.
    val shade = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.outline
    }
    val progress = remember { Animatable(1f) }
    var batch by remember { mutableStateOf(emptyList<Flake>()) }

    LaunchedEffect(burstKey) {
        if (burstKey == 0) return@LaunchedEffect
        batch = snowBatch(burstKey)
        progress.snapTo(0f)
        progress.animateTo(1f, tween(SNOW_MS, easing = LinearEasing))
    }

    Canvas(modifier.size(buttonSize)) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        val radius = buttonSize.toPx() / 2f

        batch.forEach { flake ->
            val ft = ((t - flake.delay) / (1f - flake.delay)).coerceIn(0f, 1f)
            if (ft <= 0f) return@forEach
            val ease = 1f - (1f - ft) * (1f - ft)
            val dist = radius * (0.7f + 0.8f * flake.speed * ease)
            val swing = sin(ft * 3f * PI.toFloat() + flake.phase) * flake.sway * radius * 0.18f * ft
            val x = center.x + cos(flake.angleRad) * dist + swing
            val y = center.y + sin(flake.angleRad) * dist + radius * 0.9f * ft
            val fade = (if (ft < 0.6f) 1f else 1f - (ft - 0.6f) / 0.4f).coerceIn(0f, 1f)
            val r = flake.sizeDp.dp.toPx()
            val at = Offset(x, y)
            if (shade != Color.Transparent) {
                drawCircle(shade, radius = r * 1.6f, center = at, alpha = fade * 0.25f)
            }
            drawCircle(Color.White, radius = r, center = at, alpha = fade * 0.95f)
        }
    }
}
