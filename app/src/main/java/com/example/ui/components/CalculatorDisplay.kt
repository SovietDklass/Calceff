package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalFxColors
import com.example.ui.theme.RubikFontFamily
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun CalculatorDisplay(
  expression: String,
  mainText: String,
  previewResult: String,
  isResultCommitted: Boolean,
  errorMessage: String?,
  errorTriggerId: Long,
  resultTriggerId: Long,
  isDegreeMode: Boolean,
  scientificVisible: Boolean,
  onToggleDegreeMode: () -> Unit,
  onCenterChanged: (Offset) -> Unit,
  modifier: Modifier = Modifier,
) {
  val fxColors = LocalFxColors.current
  val clipboardManager = LocalClipboardManager.current
  var copiedRecently by remember { mutableStateOf(false) }

  LaunchedEffect(copiedRecently) {
    if (copiedRecently) {
      delay(1500)
      copiedRecently = false
    }
  }

  val shakeOffset = remember { Animatable(0f) }
  LaunchedEffect(errorTriggerId) {
    if (errorTriggerId > 0L) {
      val amplitudes = listOf(-18f, 18f, -14f, 14f, -8f, 8f, 0f)
      for (amp in amplitudes) {
        shakeOffset.animateTo(amp, animationSpec = tween(38))
      }
    }
  }

  val resultScale = remember { Animatable(1f) }
  val glowSweep = remember { Animatable(0f) }
  LaunchedEffect(resultTriggerId) {
    if (resultTriggerId > 0L) {
      resultScale.snapTo(1.09f)
      resultScale.animateTo(
        targetValue = 1f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium,
        ),
      )
    }
  }
  LaunchedEffect(resultTriggerId) {
    if (resultTriggerId > 0L) {
      glowSweep.snapTo(0f)
      glowSweep.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
      )
    }
  }

  val exprScrollState = rememberScrollState()
  LaunchedEffect(expression) {
    exprScrollState.animateScrollTo(exprScrollState.maxValue)
  }

  val mainScrollState = rememberScrollState()
  LaunchedEffect(mainText) {
    mainScrollState.animateScrollTo(mainScrollState.maxValue)
  }

  val borderColor = when {
    errorMessage != null -> ErrorCrimson.copy(alpha = 0.85f)
    isResultCommitted -> fxColors.accentPrimary.copy(alpha = 0.55f)
    else -> fxColors.keyBorder.copy(alpha = 0.7f)
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
      .onGloballyPositioned { coords ->
        onCenterChanged(coords.boundsInRoot().center)
      }
      .testTag("calculator_display"),
    shape = RoundedCornerShape(28.dp),
    color = fxColors.displayContainer.copy(alpha = if (fxColors.isDark) 0.86f else 0.94f),
    border = BorderStroke(1.2.dp, borderColor),
    tonalElevation = 4.dp,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .drawBehind {
          if (glowSweep.value > 0f && glowSweep.value < 1f) {
            val sweepX = size.width * (glowSweep.value * 1.4f - 0.2f)
            drawRoundRect(
              brush = Brush.linearGradient(
                colors = listOf(
                  Color.Transparent,
                  fxColors.accentPrimary.copy(alpha = 0.24f * (1f - glowSweep.value)),
                  fxColors.accentSecondary.copy(alpha = 0.18f * (1f - glowSweep.value)),
                  Color.Transparent,
                ),
                start = Offset(sweepX - 240f, 0f),
                end = Offset(sweepX + 240f, size.height),
              ),
              cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
            )
          }
        }
        .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (scientificVisible) {
              Surface(
                onClick = onToggleDegreeMode,
                shape = RoundedCornerShape(10.dp),
                color = fxColors.accentPrimary.copy(alpha = 0.15f),
                modifier = Modifier.testTag("degree_radian_toggle"),
              ) {
                Text(
                  text = if (isDegreeMode) "DEG" else "RAD",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                  ),
                  color = fxColors.accentPrimary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
              }
            }

            AnimatedVisibility(
              visible = copiedRecently,
              enter = fadeIn(tween(150)),
              exit = fadeOut(tween(200)),
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = fxColors.accentPrimary.copy(alpha = 0.18f),
              ) {
                Text(
                  text = "Скопировано",
                  style = MaterialTheme.typography.labelMedium,
                  color = fxColors.accentPrimary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
              }
            }
          }

          IconButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(mainText))
              copiedRecently = true
            },
            modifier = Modifier
              .size(48.dp)
              .testTag("copy_result_button"),
          ) {
            Icon(
              imageVector = if (copiedRecently) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
              contentDescription = "Копировать результат",
              tint = if (copiedRecently) fxColors.accentPrimary else fxColors.textSecondary,
              modifier = Modifier.size(18.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
          contentAlignment = Alignment.CenterEnd,
        ) {
          val secondaryText = if (isResultCommitted) expression else ""
          if (secondaryText.isNotEmpty()) {
            Text(
              text = highlightExpression(secondaryText, fxColors.accentPrimary, fxColors.textSecondary),
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 18.sp,
              ),
              maxLines = 1,
              modifier = Modifier
                .horizontalScroll(exprScrollState)
                .testTag("display_secondary_expression"),
            )
          }
        }

        val mainFontSize = when {
          mainText.length > 16 -> 28.sp
          mainText.length > 12 -> 36.sp
          mainText.length > 9 -> 44.sp
          else -> 54.sp
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
              scaleX = resultScale.value
              scaleY = resultScale.value
            },
          contentAlignment = Alignment.CenterEnd,
        ) {
          val displayStyled = if (isResultCommitted) {
            AnnotatedString(mainText)
          } else {
            highlightExpression(mainText, fxColors.accentPrimary, fxColors.textPrimary)
          }
          Text(
            text = displayStyled,
            style = MaterialTheme.typography.displayLarge.copy(
              fontFamily = RubikFontFamily,
              fontSize = mainFontSize,
              lineHeight = mainFontSize * 1.15f,
              fontWeight = if (isResultCommitted) FontWeight.Bold else FontWeight.Medium,
            ),
            color = if (isResultCommitted) fxColors.accentPrimary else fxColors.textPrimary,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
              .horizontalScroll(mainScrollState)
              .testTag("display_main_value"),
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
          contentAlignment = Alignment.CenterEnd,
        ) {
          when {
            errorMessage != null -> {
              Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                ),
                color = ErrorCrimson,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("display_error_text"),
              )
            }
            previewResult.isNotEmpty() && !isResultCommitted -> {
              Text(
                text = "= $previewResult",
                style = MaterialTheme.typography.bodyLarge.copy(
                  fontFamily = JetBrainsMonoFontFamily,
                  fontSize = 20.sp,
                ),
                color = fxColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("display_preview_result"),
              )
            }
          }
        }
      }
    }
  }
}

private fun highlightExpression(
  text: String,
  operatorColor: Color,
  defaultColor: Color,
): AnnotatedString = buildAnnotatedString {
  for (ch in text) {
    if (ch in charArrayOf('+', '−', '-', '×', '÷', '%', '^', '!', '√', '(', ')')) {
      withStyle(SpanStyle(color = operatorColor, fontWeight = FontWeight.SemiBold)) {
        append(ch)
      }
    } else {
      withStyle(SpanStyle(color = defaultColor)) {
        append(ch)
      }
    }
  }
}
