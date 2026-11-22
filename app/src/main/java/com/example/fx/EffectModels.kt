package com.example.fx

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ConfettiCyan
import com.example.ui.theme.ConfettiGold
import com.example.ui.theme.ConfettiPink
import com.example.ui.theme.ConfettiViolet
import com.example.ui.theme.MatrixEmerald
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.RippleAqua
import com.example.ui.theme.RippleIndigo
import com.example.ui.theme.SparkAmber
import com.example.ui.theme.SparkCoral

enum class EffectMode(
  val storageKey: String,
  val titleRu: String,
  val subtitleRu: String,
) {
  NEON(
    storageKey = "neon",
    titleRu = "Неон",
    subtitleRu = "Свечение и лазерные импульсы",
  ),
  SPARKS(
    storageKey = "sparks",
    titleRu = "Искры",
    subtitleRu = "Огненный фейерверк при нажатии",
  ),
  RIPPLE(
    storageKey = "ripple",
    titleRu = "Волна",
    subtitleRu = "Жидкая рябь от каждой клавиши",
  ),
  MATRIX(
    storageKey = "matrix",
    titleRu = "Матрица",
    subtitleRu = "Цифровой поток символов",
  ),
  CONFETTI(
    storageKey = "confetti",
    titleRu = "Конфетти",
    subtitleRu = "Яркий салют на каждое вычисление",
  );

  fun primaryColor(darkTheme: Boolean): Color = when (this) {
    NEON -> if (darkTheme) NeonCyan else Color(0xFF0099B8)
    SPARKS -> if (darkTheme) SparkAmber else Color(0xFFE07A00)
    RIPPLE -> if (darkTheme) RippleAqua else Color(0xFF0284C7)
    MATRIX -> if (darkTheme) MatrixGreen else Color(0xFF059669)
    CONFETTI -> if (darkTheme) ConfettiPink else Color(0xFFE11D48)
  }

  fun secondaryColor(darkTheme: Boolean): Color = when (this) {
    NEON -> if (darkTheme) NeonMagenta else Color(0xFFD91B76)
    SPARKS -> if (darkTheme) SparkCoral else Color(0xFFEA3C1A)
    RIPPLE -> if (darkTheme) RippleIndigo else Color(0xFF4F46E5)
    MATRIX -> if (darkTheme) MatrixEmerald else Color(0xFF047857)
    CONFETTI -> if (darkTheme) ConfettiGold else Color(0xFFD97706)
  }

  fun particlePalette(): List<Color> = when (this) {
    NEON -> listOf(NeonCyan, NeonMagenta, Color(0xFF7C3AED), Color.White)
    SPARKS -> listOf(SparkAmber, SparkCoral, Color(0xFFFFE57F), Color(0xFFFF9100))
    RIPPLE -> listOf(RippleAqua, RippleIndigo, Color(0xFF67E8F9), Color(0xFFC084FC))
    MATRIX -> listOf(MatrixGreen, MatrixEmerald, Color(0xFF86EFAC), Color(0xFF22C55E))
    CONFETTI -> listOf(ConfettiPink, ConfettiGold, ConfettiCyan, ConfettiViolet, MatrixGreen)
  }

  companion object {
    fun fromKey(key: String?): EffectMode =
      entries.firstOrNull { it.storageKey == key } ?: NEON
  }
}

enum class FxBurstKind {
  DIGIT_TAP,
  OPERATOR_TAP,
  EQUALS_CELEBRATION,
  CLEAR_SWEEP,
  ERROR_GLITCH,
}

data class FxBurstEvent(
  val id: Long,
  val kind: FxBurstKind,
  val originX: Float,
  val originY: Float,
  val label: String = "",
)
