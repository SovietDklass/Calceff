package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fx.EffectMode
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalFxColors
import com.example.ui.theme.RubikFontFamily

enum class KeyKind {
  DIGIT,
  UTILITY,
  OPERATOR,
  EQUALS,
  CLEAR,
  SCIENTIFIC,
}

@Composable
fun ScientificKeypadStrip(
  visible: Boolean,
  hapticsEnabled: Boolean,
  effectMode: EffectMode,
  onScientificToken: (String, Offset) -> Unit,
  onParenthesis: (String, Offset) -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = visible,
    enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
    exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut(),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ScientificChipKey("sin", "Синус", "key_sin", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("sin(", it)
        }
        ScientificChipKey("cos", "Косинус", "key_cos", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("cos(", it)
        }
        ScientificChipKey("tan", "Тангенс", "key_tan", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("tan(", it)
        }
        ScientificChipKey("√", "Квадратный корень", "key_sqrt", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("√(", it)
        }
        ScientificChipKey("x²", "Квадрат числа", "key_sq", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("x²", it)
        }
        ScientificChipKey("^", "Степень", "key_pow", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("^", it)
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ScientificChipKey("ln", "Натуральный логарифм", "key_ln", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("ln(", it)
        }
        ScientificChipKey("(", "Открыть скобку", "key_lparen", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onParenthesis("(", it)
        }
        ScientificChipKey(")", "Закрыть скобку", "key_rparen", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onParenthesis(")", it)
        }
        ScientificChipKey("π", "Число Пи", "key_pi", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("π", it)
        }
        ScientificChipKey("e", "Число e", "key_e", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("e", it)
        }
        ScientificChipKey("!", "Факториал", "key_fact", hapticsEnabled, effectMode, Modifier.weight(1f)) {
          onScientificToken("!", it)
        }
      }
    }
  }
}

@Composable
private fun ScientificChipKey(
  label: String,
  description: String,
  testTagName: String,
  hapticsEnabled: Boolean,
  effectMode: EffectMode,
  modifier: Modifier = Modifier,
  onClick: (Offset) -> Unit,
) {
  FxKeyButton(
    label = label,
    contentDescriptionText = description,
    testTagName = testTagName,
    kind = KeyKind.SCIENTIFIC,
    hapticsEnabled = hapticsEnabled,
    effectMode = effectMode,
    cornerRadius = 16.dp,
    modifier = modifier.height(48.dp),
    onClick = onClick,
  )
}

@Composable
fun CalculatorKeypad(
  hapticsEnabled: Boolean,
  effectMode: EffectMode,
  isClearC: Boolean,
  onDigit: (String, Offset) -> Unit,
  onDecimal: (Offset) -> Unit,
  onOperator: (String, Offset) -> Unit,
  onPercent: (Offset) -> Unit,
  onToggleSign: (Offset) -> Unit,
  onBackspace: (Offset) -> Unit,
  onClearAll: (Offset) -> Unit,
  onEquals: (Offset) -> Unit,
  modifier: Modifier = Modifier,
) {
  val spacing = 10.dp

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(spacing),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      FxKeyButton(
        label = if (isClearC) "C" else "AC",
        contentDescriptionText = "Очистить всё",
        testTagName = "key_clear",
        kind = KeyKind.CLEAR,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = onClearAll,
      )
      FxKeyButton(
        label = "",
        icon = Icons.AutoMirrored.Rounded.Backspace,
        contentDescriptionText = "Удалить последний символ",
        testTagName = "key_backspace",
        kind = KeyKind.UTILITY,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = onBackspace,
      )
      FxKeyButton(
        label = "%",
        contentDescriptionText = "Процент",
        testTagName = "key_percent",
        kind = KeyKind.UTILITY,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = onPercent,
      )
      FxKeyButton(
        label = "÷",
        contentDescriptionText = "Разделить",
        testTagName = "key_divide",
        kind = KeyKind.OPERATOR,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = { onOperator("÷", it) },
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      listOf("7", "8", "9").forEach { d ->
        FxKeyButton(
          label = d,
          contentDescriptionText = "Цифра $d",
          testTagName = "key_$d",
          kind = KeyKind.DIGIT,
          hapticsEnabled = hapticsEnabled,
          effectMode = effectMode,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          onClick = { onDigit(d, it) },
        )
      }
      FxKeyButton(
        label = "×",
        contentDescriptionText = "Умножить",
        testTagName = "key_multiply",
        kind = KeyKind.OPERATOR,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = { onOperator("×", it) },
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      listOf("4", "5", "6").forEach { d ->
        FxKeyButton(
          label = d,
          contentDescriptionText = "Цифра $d",
          testTagName = "key_$d",
          kind = KeyKind.DIGIT,
          hapticsEnabled = hapticsEnabled,
          effectMode = effectMode,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          onClick = { onDigit(d, it) },
        )
      }
      FxKeyButton(
        label = "−",
        contentDescriptionText = "Вычесть",
        testTagName = "key_minus",
        kind = KeyKind.OPERATOR,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = { onOperator("−", it) },
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      listOf("1", "2", "3").forEach { d ->
        FxKeyButton(
          label = d,
          contentDescriptionText = "Цифра $d",
          testTagName = "key_$d",
          kind = KeyKind.DIGIT,
          hapticsEnabled = hapticsEnabled,
          effectMode = effectMode,
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          onClick = { onDigit(d, it) },
        )
      }
      FxKeyButton(
        label = "+",
        contentDescriptionText = "Сложить",
        testTagName = "key_plus",
        kind = KeyKind.OPERATOR,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = { onOperator("+", it) },
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      FxKeyButton(
        label = "+/−",
        contentDescriptionText = "Сменить знак",
        testTagName = "key_sign",
        kind = KeyKind.DIGIT,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = onToggleSign,
      )
      FxKeyButton(
        label = "0",
        contentDescriptionText = "Цифра 0",
        testTagName = "key_0",
        kind = KeyKind.DIGIT,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = { onDigit("0", it) },
      )
      FxKeyButton(
        label = ".",
        contentDescriptionText = "Десятичная точка",
        testTagName = "key_decimal",
        kind = KeyKind.DIGIT,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = onDecimal,
      )
      FxKeyButton(
        label = "=",
        contentDescriptionText = "Вычислить результат",
        testTagName = "key_equals",
        kind = KeyKind.EQUALS,
        hapticsEnabled = hapticsEnabled,
        effectMode = effectMode,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        onClick = onEquals,
      )
    }
  }
}

@Composable
fun FxKeyButton(
  label: String,
  contentDescriptionText: String,
  testTagName: String,
  kind: KeyKind,
  hapticsEnabled: Boolean,
  effectMode: EffectMode,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  cornerRadius: Dp = 22.dp,
  onClick: (Offset) -> Unit,
) {
  val fxColors = LocalFxColors.current
  val view = LocalView.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val isHovered by interactionSource.collectIsHoveredAsState()
  var centerOffset by remember { mutableStateOf(Offset.Zero) }

  val scale by animateFloatAsState(
    targetValue = when {
      isPressed -> 0.90f
      isHovered -> 1.02f
      else -> 1f
    },
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium,
    ),
    label = "keyScale",
  )

  val shape = RoundedCornerShape(cornerRadius)

  val containerColor = when (kind) {
    KeyKind.DIGIT -> fxColors.keyDigitContainer
    KeyKind.UTILITY -> fxColors.keyUtilityContainer
    KeyKind.OPERATOR -> fxColors.keyOperatorContainer
    KeyKind.CLEAR -> ErrorCrimson.copy(alpha = if (fxColors.isDark) 0.18f else 0.12f)
    KeyKind.SCIENTIFIC -> fxColors.keyUtilityContainer.copy(alpha = 0.88f)
    KeyKind.EQUALS -> fxColors.accentPrimary
  }

  val contentColor = when (kind) {
    KeyKind.DIGIT -> fxColors.textPrimary
    KeyKind.UTILITY -> fxColors.textPrimary
    KeyKind.OPERATOR -> fxColors.accentPrimary
    KeyKind.CLEAR -> ErrorCrimson
    KeyKind.SCIENTIFIC -> fxColors.textPrimary
    KeyKind.EQUALS -> Color.White
  }

  val borderStroke = when {
    kind == KeyKind.EQUALS -> BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
    isPressed -> BorderStroke(1.5.dp, fxColors.accentPrimary.copy(alpha = 0.9f))
    effectMode == EffectMode.NEON && kind == KeyKind.OPERATOR ->
      BorderStroke(1.2.dp, fxColors.accentPrimary.copy(alpha = 0.55f))
    effectMode == EffectMode.NEON ->
      BorderStroke(1.dp, fxColors.keyBorder.copy(alpha = 0.8f))
    else -> BorderStroke(1.dp, fxColors.keyBorder.copy(alpha = 0.45f))
  }

  val equalsBrush = Brush.linearGradient(
    colors = listOf(fxColors.accentPrimary, fxColors.accentSecondary),
  )

  val shadowModifier = if (kind == KeyKind.EQUALS) {
    Modifier.shadow(
      elevation = if (isPressed) 2.dp else 10.dp,
      shape = shape,
      ambientColor = fxColors.accentPrimary,
      spotColor = fxColors.accentSecondary,
    )
  } else {
    Modifier
  }

  Surface(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .then(shadowModifier)
      .clip(shape)
      .hoverable(interactionSource = interactionSource)
      .onGloballyPositioned { coords ->
        centerOffset = coords.boundsInRoot().center
      }
      .semantics { contentDescription = contentDescriptionText }
      .testTag(testTagName)
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(
          bounded = true,
          color = if (kind == KeyKind.EQUALS) Color.White else fxColors.accentPrimary,
        ),
        role = Role.Button,
        onClick = {
          if (hapticsEnabled) {
            val feedbackConst = when (kind) {
              KeyKind.EQUALS -> HapticFeedbackConstants.CONTEXT_CLICK
              else -> HapticFeedbackConstants.KEYBOARD_TAP
            }
            view.performHapticFeedback(feedbackConst)
          }
          onClick(centerOffset)
        },
      ),
    shape = shape,
    color = if (kind == KeyKind.EQUALS) Color.Transparent else containerColor,
    border = borderStroke,
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .then(
          if (kind == KeyKind.EQUALS) {
            Modifier.background(equalsBrush)
          } else if (isPressed) {
            Modifier.background(fxColors.accentPrimary.copy(alpha = 0.14f))
          } else {
            Modifier
          },
        ),
      contentAlignment = Alignment.Center,
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = contentDescriptionText,
          tint = contentColor,
          modifier = Modifier.padding(8.dp),
        )
      } else {
        val fontSize = when (kind) {
          KeyKind.SCIENTIFIC -> 15.sp
          KeyKind.OPERATOR, KeyKind.EQUALS -> 28.sp
          KeyKind.CLEAR -> 20.sp
          else -> if (label.length > 2) 19.sp else 25.sp
        }
        Text(
          text = label,
          style = MaterialTheme.typography.labelLarge.copy(
            fontFamily = if (kind == KeyKind.SCIENTIFIC) JetBrainsMonoFontFamily else RubikFontFamily,
            fontSize = fontSize,
            fontWeight = if (kind == KeyKind.OPERATOR || kind == KeyKind.EQUALS) {
              FontWeight.SemiBold
            } else {
              FontWeight.Medium
            },
          ),
          color = contentColor,
          maxLines = 1,
        )
      }
    }
  }
}
