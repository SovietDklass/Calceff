package com.example.fx

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundFxPlayer {

  private val sampleRate = 22050
  private val executor: ExecutorService = Executors.newSingleThreadExecutor()

  @Volatile
  private var released = false

  fun play(kind: FxBurstKind, label: String = "", mode: EffectMode = EffectMode.NEON) {
    if (released) return
    executor.execute {
      if (released) return@execute
      try {
        val pcm = synthesizePcm(kind, label, mode)
        playPcm(pcm)
      } catch (_: Throwable) {
        // Ignore audio device unavailability (e.g., headless test environments)
      }
    }
  }

  fun release() {
    released = true
    executor.shutdownNow()
  }

  private fun synthesizePcm(kind: FxBurstKind, label: String, mode: EffectMode): ShortArray {
    val modePitchScale = when (mode) {
      EffectMode.NEON -> 1.0f
      EffectMode.SPARKS -> 1.18f
      EffectMode.RIPPLE -> 0.88f
      EffectMode.MATRIX -> 1.32f
      EffectMode.CONFETTI -> 1.08f
    }

    return when (kind) {
      FxBurstKind.DIGIT_TAP -> {
        val digitIndex = label.singleOrNull()?.digitToIntOrNull() ?: 5
        val pentatonic = floatArrayOf(392f, 440f, 493.88f, 523.25f, 587.33f, 659.25f, 698.46f, 783.99f, 880f, 987.77f)
        val freq = pentatonic[digitIndex.coerceIn(0, 9)] * modePitchScale
        generateTone(freq = freq, durationMs = 46, decay = 9.0f, harmonic = 0.22f, volume = 0.28f)
      }
      FxBurstKind.OPERATOR_TAP -> {
        val baseFreq = when (mode) {
          EffectMode.RIPPLE -> 520f
          EffectMode.MATRIX -> 860f
          else -> 680f
        }
        generateSweep(startFreq = baseFreq, endFreq = baseFreq * 1.25f, durationMs = 58, volume = 0.30f)
      }
      FxBurstKind.CLEAR_SWEEP -> {
        generateSweep(startFreq = 540f * modePitchScale, endFreq = 260f * modePitchScale, durationMs = 75, volume = 0.26f)
      }
      FxBurstKind.EQUALS_CELEBRATION -> {
        generateChord(
          freqs = floatArrayOf(523.25f * modePitchScale, 659.25f * modePitchScale, 783.99f * modePitchScale),
          durationMs = 170,
          volume = 0.34f,
        )
      }
      FxBurstKind.ERROR_GLITCH -> {
        generateSweep(startFreq = 240f, endFreq = 135f, durationMs = 140, volume = 0.35f)
      }
    }
  }

  private fun generateTone(
    freq: Float,
    durationMs: Int,
    decay: Float,
    harmonic: Float,
    volume: Float,
  ): ShortArray {
    val count = (sampleRate * durationMs) / 1000
    val out = ShortArray(count)
    for (i in 0 until count) {
      val t = i.toFloat() / sampleRate
      val env = exp(-decay * t) * fadeEdges(i, count)
      val wave = sin(2.0 * PI * freq * t) + harmonic * sin(2.0 * PI * freq * 2.0 * t)
      out[i] = (wave * env * volume * Short.MAX_VALUE).toInt()
        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return out
  }

  private fun generateSweep(
    startFreq: Float,
    endFreq: Float,
    durationMs: Int,
    volume: Float,
  ): ShortArray {
    val count = (sampleRate * durationMs) / 1000
    val out = ShortArray(count)
    var phase = 0.0
    for (i in 0 until count) {
      val progress = i.toFloat() / count.coerceAtLeast(1)
      val freq = startFreq + (endFreq - startFreq) * progress
      phase += 2.0 * PI * freq / sampleRate
      val env = (1f - progress * 0.65f) * fadeEdges(i, count)
      out[i] = (sin(phase) * env * volume * Short.MAX_VALUE).toInt()
        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return out
  }

  private fun generateChord(
    freqs: FloatArray,
    durationMs: Int,
    volume: Float,
  ): ShortArray {
    val count = (sampleRate * durationMs) / 1000
    val out = ShortArray(count)
    val invSize = 1f / freqs.size.coerceAtLeast(1)
    for (i in 0 until count) {
      val t = i.toFloat() / sampleRate
      val env = exp(-6.5f * t) * fadeEdges(i, count)
      var sum = 0.0
      for (idx in freqs.indices) {
        val staggerStart = idx * 0.018f
        if (t >= staggerStart) {
          sum += sin(2.0 * PI * freqs[idx] * (t - staggerStart))
        }
      }
      out[i] = (sum * invSize * env * volume * Short.MAX_VALUE).toInt()
        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return out
  }

  private fun fadeEdges(index: Int, total: Int): Float {
    val attack = 40f
    val release = 80f
    val inFactor = (index / attack).coerceIn(0f, 1f)
    val outFactor = ((total - 1 - index) / release).coerceIn(0f, 1f)
    return minOf(inFactor, outFactor)
  }

  private fun playPcm(pcm: ShortArray) {
    if (pcm.isEmpty()) return
    val minBuf = AudioTrack.getMinBufferSize(
      sampleRate,
      AudioFormat.CHANNEL_OUT_MONO,
      AudioFormat.ENCODING_PCM_16BIT,
    )
    val bufferSizeBytes = maxOf(minBuf, pcm.size * 2)
    val track = AudioTrack.Builder()
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build(),
      )
      .setAudioFormat(
        AudioFormat.Builder()
          .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(sampleRate)
          .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
          .build(),
      )
      .setTransferMode(AudioTrack.MODE_STATIC)
      .setBufferSizeInBytes(bufferSizeBytes)
      .build()

    try {
      track.write(pcm, 0, pcm.size)
      track.play()
      val waitMs = ((pcm.size.toLong() * 1000L) / sampleRate) + 15L
      Thread.sleep(waitMs)
    } finally {
      runCatching { track.stop() }
      runCatching { track.release() }
    }
  }
}
