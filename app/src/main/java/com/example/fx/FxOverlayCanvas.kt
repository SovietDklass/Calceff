package com.example.fx

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalFxColors
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class ParticleShape {
  CIRCLE,
  SPARK_LINE,
  CONFETTI_RECT,
  MATRIX_GLYPH,
}

private data class FxParticle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var gravity: Float,
  var drag: Float,
  var size: Float,
  var rotation: Float,
  var angularVel: Float,
  var age: Float,
  var lifetime: Float,
  val color: Color,
  val shape: ParticleShape,
  val glyph: String = "",
  val flutterPhase: Float = 0f,
)

private data class FxShockwave(
  val x: Float,
  val y: Float,
  var radius: Float,
  val maxRadius: Float,
  val strokeWidth: Float,
  var age: Float,
  val lifetime: Float,
  val color: Color,
  val doubleRing: Boolean = false,
)

private val MATRIX_GLYPHS = arrayOf("0", "1", "Σ", "π", "Δ", "λ", "7", "9", "∞", "√")

@Composable
fun FxAmbientBackground(
  effectMode: EffectMode,
  resultTriggerId: Long,
  modifier: Modifier = Modifier,
) {
  val fxColors = LocalFxColors.current
  val infiniteTransition = rememberInfiniteTransition(label = "ambientBg")
  val drift by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2 * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 9000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "drift",
  )

  val pulseAnim = remember { Animatable(0f) }
  LaunchedEffect(resultTriggerId) {
    if (resultTriggerId > 0L) {
      pulseAnim.snapTo(1f)
      pulseAnim.animateTo(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
      )
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(fxColors.backgroundTop, fxColors.backgroundBottom),
      ),
    )

    val orbAlpha = if (fxColors.isDark) 0.17f else 0.14f
    val pulseBoost = pulseAnim.value * 0.18f

    val orb1Center = Offset(
      x = size.width * (0.25f + 0.14f * cos(drift)),
      y = size.height * (0.22f + 0.08f * sin(drift)),
    )
    val orb1Radius = size.minDimension * (0.68f + pulseAnim.value * 0.22f)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          fxColors.accentPrimary.copy(alpha = orbAlpha + pulseBoost),
          Color.Transparent,
        ),
        center = orb1Center,
        radius = orb1Radius,
      ),
      center = orb1Center,
      radius = orb1Radius,
    )

    val orb2Center = Offset(
      x = size.width * (0.78f + 0.12f * sin(drift * 0.8f)),
      y = size.height * (0.68f + 0.10f * cos(drift * 0.9f)),
    )
    val orb2Radius = size.minDimension * (0.62f + pulseAnim.value * 0.18f)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          fxColors.accentSecondary.copy(alpha = (orbAlpha * 0.85f) + pulseBoost * 0.8f),
          Color.Transparent,
        ),
        center = orb2Center,
        radius = orb2Radius,
      ),
      center = orb2Center,
      radius = orb2Radius,
    )

    if (effectMode == EffectMode.MATRIX) {
      val gridAlpha = if (fxColors.isDark) 0.055f else 0.045f
      val step = 56f
      var x = 0f
      while (x < size.width) {
        drawLine(
          color = fxColors.accentPrimary.copy(alpha = gridAlpha),
          start = Offset(x, 0f),
          end = Offset(x, size.height),
          strokeWidth = 1f,
        )
        x += step
      }
      var y = 0f
      while (y < size.height) {
        drawLine(
          color = fxColors.accentPrimary.copy(alpha = gridAlpha),
          start = Offset(0f, y),
          end = Offset(size.width, y),
          strokeWidth = 1f,
        )
        y += step
      }
    }
  }
}

@Composable
fun FxOverlayCanvas(
  burstEvents: SharedFlow<FxBurstEvent>,
  effectMode: EffectMode,
  intensity: Float,
  modifier: Modifier = Modifier,
) {
  val particles = remember { mutableStateListOf<FxParticle>() }
  val shockwaves = remember { mutableStateListOf<FxShockwave>() }
  var frameTick by remember { mutableLongStateOf(0L) }
  val textMeasurer = rememberTextMeasurer()

  LaunchedEffect(effectMode, intensity) {
    burstEvents.collect { event ->
      spawnBurst(
        event = event,
        mode = effectMode,
        intensity = intensity,
        particles = particles,
        shockwaves = shockwaves,
      )
      frameTick++
    }
  }

  LaunchedEffect(frameTick > 0L, particles.isNotEmpty(), shockwaves.isNotEmpty()) {
    if (particles.isEmpty() && shockwaves.isEmpty()) return@LaunchedEffect
    var lastNanos = 0L
    while (particles.isNotEmpty() || shockwaves.isNotEmpty()) {
      withFrameNanos { nanos ->
        val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastNanos = nanos

        var pIdx = particles.lastIndex
        while (pIdx >= 0) {
          val p = particles[pIdx]
          p.age += dt
          if (p.age >= p.lifetime) {
            particles.removeAt(pIdx)
          } else {
            p.vx *= (1f - p.drag * dt).coerceAtLeast(0.1f)
            p.vy = p.vy * (1f - p.drag * dt).coerceAtLeast(0.1f) + p.gravity * dt
            val flutter = if (p.shape == ParticleShape.CONFETTI_RECT) {
              sin(p.age * 11f + p.flutterPhase) * 45f * dt
            } else {
              0f
            }
            p.x += p.vx * dt + flutter
            p.y += p.vy * dt
            p.rotation += p.angularVel * dt
          }
          pIdx--
        }

        var wIdx = shockwaves.lastIndex
        while (wIdx >= 0) {
          val w = shockwaves[wIdx]
          w.age += dt
          if (w.age >= w.lifetime) {
            shockwaves.removeAt(wIdx)
          } else {
            val t = (w.age / w.lifetime).coerceIn(0f, 1f)
            val eased = 1f - (1f - t) * (1f - t) * (1f - t)
            w.radius = w.maxRadius * eased
          }
          wIdx--
        }
        frameTick++
      }
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    val tickRead = frameTick
    if (tickRead < 0L) return@Canvas

    for (wave in shockwaves) {
      val progress = (wave.age / wave.lifetime).coerceIn(0f, 1f)
      val alpha = (1f - progress) * (1f - progress) * 0.85f
      if (alpha > 0.01f) {
        drawCircle(
          color = wave.color.copy(alpha = alpha),
          radius = wave.radius,
          center = Offset(wave.x, wave.y),
          style = Stroke(width = wave.strokeWidth * (1f - progress * 0.55f)),
        )
        if (wave.doubleRing && wave.radius > 24f) {
          drawCircle(
            color = wave.color.copy(alpha = alpha * 0.45f),
            radius = wave.radius * 0.68f,
            center = Offset(wave.x, wave.y),
            style = Stroke(width = wave.strokeWidth * 0.6f),
          )
        }
      }
    }

    for (p in particles) {
      val progress = (p.age / p.lifetime).coerceIn(0f, 1f)
      val alpha = (1f - progress * progress).coerceIn(0f, 1f)
      if (alpha <= 0.01f) continue
      drawParticle(p, alpha, textMeasurer)
    }
  }
}

private fun DrawScope.drawParticle(
  p: FxParticle,
  alpha: Float,
  textMeasurer: TextMeasurer,
) {
  when (p.shape) {
    ParticleShape.CIRCLE -> {
      drawCircle(
        color = p.color.copy(alpha = alpha * 0.30f),
        radius = p.size * 2.1f,
        center = Offset(p.x, p.y),
      )
      drawCircle(
        color = p.color.copy(alpha = alpha),
        radius = p.size * (1f - (p.age / p.lifetime) * 0.45f),
        center = Offset(p.x, p.y),
      )
    }
    ParticleShape.SPARK_LINE -> {
      val tailX = p.x - p.vx * 0.036f
      val tailY = p.y - p.vy * 0.036f
      drawLine(
        color = p.color.copy(alpha = alpha * 0.45f),
        start = Offset(tailX, tailY),
        end = Offset(p.x, p.y),
        strokeWidth = p.size * 1.9f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = Color.White.copy(alpha = alpha * 0.9f),
        start = Offset((tailX + p.x) * 0.5f, (tailY + p.y) * 0.5f),
        end = Offset(p.x, p.y),
        strokeWidth = p.size * 0.85f,
        cap = StrokeCap.Round,
      )
    }
    ParticleShape.CONFETTI_RECT -> {
      val scaleY = kotlin.math.abs(cos(p.age * 9f + p.flutterPhase)).coerceAtLeast(0.25f)
      val w = p.size * 1.6f
      val h = p.size * 0.9f * scaleY
      rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
        drawRect(
          color = p.color.copy(alpha = alpha),
          topLeft = Offset(p.x - w / 2f, p.y - h / 2f),
          size = Size(w, h),
        )
      }
    }
    ParticleShape.MATRIX_GLYPH -> {
      val measured = textMeasurer.measure(
        text = p.glyph,
        style = TextStyle(
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = p.size.sp,
          color = p.color.copy(alpha = alpha),
        ),
      )
      drawText(
        textLayoutResult = measured,
        topLeft = Offset(p.x - measured.size.width / 2f, p.y - measured.size.height / 2f),
      )
    }
  }
}

private fun spawnBurst(
  event: FxBurstEvent,
  mode: EffectMode,
  intensity: Float,
  particles: MutableList<FxParticle>,
  shockwaves: MutableList<FxShockwave>,
) {
  if (particles.size > 260) {
    repeat(particles.size - 220) {
      if (particles.isNotEmpty()) particles.removeAt(0)
    }
  }

  val palette = if (event.kind == FxBurstKind.ERROR_GLITCH) {
    listOf(ErrorCrimson, Color(0xFFFF7B7B), Color.White)
  } else {
    mode.particlePalette()
  }

  val countMultiplier = when (event.kind) {
    FxBurstKind.DIGIT_TAP -> 1.0f
    FxBurstKind.OPERATOR_TAP -> 1.35f
    FxBurstKind.EQUALS_CELEBRATION -> 3.4f
    FxBurstKind.CLEAR_SWEEP -> 1.5f
    FxBurstKind.ERROR_GLITCH -> 1.8f
  } * intensity

  if (event.kind == FxBurstKind.ERROR_GLITCH) {
    shockwaves.add(
      FxShockwave(
        x = event.originX,
        y = event.originY,
        radius = 12f,
        maxRadius = 320f * intensity,
        strokeWidth = 9f,
        age = 0f,
        lifetime = 0.42f,
        color = ErrorCrimson,
        doubleRing = true,
      ),
    )
    val count = (16 * intensity).toInt().coerceAtLeast(6)
    repeat(count) {
      val angle = if (Random.nextBoolean()) 0f else PI.toFloat()
      val spread = (Random.nextFloat() - 0.5f) * 0.45f
      val speed = Random.nextFloat() * 520f + 220f
      particles.add(
        FxParticle(
          x = event.originX + (Random.nextFloat() - 0.5f) * 140f,
          y = event.originY + (Random.nextFloat() - 0.5f) * 40f,
          vx = cos(angle + spread) * speed,
          vy = sin(angle + spread) * speed,
          gravity = 0f,
          drag = 3.5f,
          size = Random.nextFloat() * 4f + 3f,
          rotation = 0f,
          angularVel = 0f,
          age = 0f,
          lifetime = 0.38f,
          color = palette.random(),
          shape = ParticleShape.SPARK_LINE,
        ),
      )
    }
    return
  }

  when (mode) {
    EffectMode.NEON -> {
      val maxWave = when (event.kind) {
        FxBurstKind.EQUALS_CELEBRATION -> 520f
        FxBurstKind.OPERATOR_TAP -> 210f
        else -> 150f
      } * intensity
      shockwaves.add(
        FxShockwave(
          x = event.originX,
          y = event.originY,
          radius = 10f,
          maxRadius = maxWave,
          strokeWidth = if (event.kind == FxBurstKind.EQUALS_CELEBRATION) 10f else 5.5f,
          age = 0f,
          lifetime = if (event.kind == FxBurstKind.EQUALS_CELEBRATION) 0.62f else 0.40f,
          color = palette.first(),
          doubleRing = event.kind == FxBurstKind.EQUALS_CELEBRATION,
        ),
      )
      val count = (10 * countMultiplier).toInt().coerceIn(5, 48)
      repeat(count) {
        val angle = Random.nextFloat() * (2 * PI).toFloat()
        val speed = (Random.nextFloat() * 340f + 160f) *
          if (event.kind == FxBurstKind.EQUALS_CELEBRATION) 1.55f else 1.0f
        particles.add(
          FxParticle(
            x = event.originX,
            y = event.originY,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            gravity = 120f,
            drag = 2.6f,
            size = Random.nextFloat() * 3.5f + 2.5f,
            rotation = 0f,
            angularVel = 0f,
            age = 0f,
            lifetime = Random.nextFloat() * 0.30f + 0.32f,
            color = palette.random(),
            shape = ParticleShape.SPARK_LINE,
          ),
        )
      }
    }

    EffectMode.SPARKS -> {
      if (event.kind == FxBurstKind.EQUALS_CELEBRATION) {
        shockwaves.add(
          FxShockwave(
            x = event.originX,
            y = event.originY,
            radius = 8f,
            maxRadius = 380f * intensity,
            strokeWidth = 8f,
            age = 0f,
            lifetime = 0.45f,
            color = palette.first(),
          ),
        )
      }
      val count = (14 * countMultiplier).toInt().coerceIn(8, 60)
      repeat(count) {
        val angle = Random.nextFloat() * (2 * PI).toFloat()
        val speed = (Random.nextFloat() * 440f + 140f) *
          if (event.kind == FxBurstKind.EQUALS_CELEBRATION) 1.4f else 1.0f
        particles.add(
          FxParticle(
            x = event.originX,
            y = event.originY,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed - 180f,
            gravity = 880f,
            drag = 1.4f,
            size = Random.nextFloat() * 3.5f + 2.2f,
            rotation = 0f,
            angularVel = 0f,
            age = 0f,
            lifetime = Random.nextFloat() * 0.45f + 0.40f,
            color = palette.random(),
            shape = ParticleShape.SPARK_LINE,
          ),
        )
      }
    }

    EffectMode.RIPPLE -> {
      val waveCount = if (event.kind == FxBurstKind.EQUALS_CELEBRATION) 3 else 2
      repeat(waveCount) { idx ->
        shockwaves.add(
          FxShockwave(
            x = event.originX,
            y = event.originY,
            radius = 6f,
            maxRadius = (if (event.kind == FxBurstKind.EQUALS_CELEBRATION) 640f else 240f) *
              (1f - idx * 0.22f) * intensity,
            strokeWidth = 6f - idx * 1.5f,
            age = -idx * 0.06f,
            lifetime = 0.65f,
            color = palette[idx % palette.size],
            doubleRing = false,
          ),
        )
      }
      val droplets = (6 * countMultiplier).toInt().coerceIn(4, 24)
      repeat(droplets) {
        val angle = Random.nextFloat() * (2 * PI).toFloat()
        val speed = Random.nextFloat() * 190f + 80f
        particles.add(
          FxParticle(
            x = event.originX,
            y = event.originY,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            gravity = 150f,
            drag = 2.2f,
            size = Random.nextFloat() * 4.5f + 3f,
            rotation = 0f,
            angularVel = 0f,
            age = 0f,
            lifetime = 0.52f,
            color = palette.random(),
            shape = ParticleShape.CIRCLE,
          ),
        )
      }
    }

    EffectMode.MATRIX -> {
      val count = (9 * countMultiplier).toInt().coerceIn(5, 42)
      repeat(count) {
        val spreadX = (Random.nextFloat() - 0.5f) *
          if (event.kind == FxBurstKind.EQUALS_CELEBRATION) 420f else 150f
        particles.add(
          FxParticle(
            x = event.originX + spreadX * 0.35f,
            y = event.originY + (Random.nextFloat() - 0.5f) * 30f,
            vx = spreadX * 0.7f,
            vy = Random.nextFloat() * 240f + 90f,
            gravity = 260f,
            drag = 0.8f,
            size = Random.nextFloat() * 6f + 13f,
            rotation = 0f,
            angularVel = 0f,
            age = 0f,
            lifetime = Random.nextFloat() * 0.45f + 0.55f,
            color = palette.random(),
            shape = ParticleShape.MATRIX_GLYPH,
            glyph = if (event.label.isNotEmpty() && Random.nextFloat() < 0.35f) {
              event.label
            } else {
              MATRIX_GLYPHS.random()
            },
          ),
        )
      }
      if (event.kind == FxBurstKind.EQUALS_CELEBRATION) {
        shockwaves.add(
          FxShockwave(
            x = event.originX,
            y = event.originY,
            radius = 8f,
            maxRadius = 420f * intensity,
            strokeWidth = 4f,
            age = 0f,
            lifetime = 0.45f,
            color = palette.first(),
          ),
        )
      }
    }

    EffectMode.CONFETTI -> {
      val count = (12 * countMultiplier).toInt().coerceIn(6, 56)
      repeat(count) {
        val angle = (-PI / 2.0 + (Random.nextFloat() - 0.5f) * 2.1).toFloat()
        val speed = (Random.nextFloat() * 480f + 210f) *
          if (event.kind == FxBurstKind.EQUALS_CELEBRATION) 1.45f else 1.0f
        particles.add(
          FxParticle(
            x = event.originX,
            y = event.originY,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            gravity = 760f,
            drag = 1.6f,
            size = Random.nextFloat() * 5.5f + 5.5f,
            rotation = Random.nextFloat() * 360f,
            angularVel = (Random.nextFloat() - 0.5f) * 520f,
            age = 0f,
            lifetime = Random.nextFloat() * 0.55f + 0.65f,
            color = palette.random(),
            shape = ParticleShape.CONFETTI_RECT,
            flutterPhase = Random.nextFloat() * 6.28f,
          ),
        )
      }
      if (event.kind == FxBurstKind.EQUALS_CELEBRATION) {
        shockwaves.add(
          FxShockwave(
            x = event.originX,
            y = event.originY,
            radius = 12f,
            maxRadius = 460f * intensity,
            strokeWidth = 7f,
            age = 0f,
            lifetime = 0.50f,
            color = palette.first(),
          ),
        )
      }
    }
  }
}
