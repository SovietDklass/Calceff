package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fx.FxAmbientBackground
import com.example.fx.FxOverlayCanvas
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.EffectsSettingsSheet
import com.example.ui.components.FxModePillBar
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.ScientificKeypadStrip
import com.example.ui.theme.LocalFxColors

@Composable
fun CalculatorScreen(
  viewModel: CalculatorViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val prefs = uiState.preferences

  var showEffectsSheet by remember { mutableStateOf(false) }
  var showHistorySheet by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize()) {
    FxAmbientBackground(
      effectMode = prefs.effectMode,
      resultTriggerId = uiState.resultTriggerId,
    )

    Scaffold(
      containerColor = Color.Transparent,
      contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
      BoxWithConstraints(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 16.dp, vertical = 8.dp),
      ) {
        val isWideLayout = maxWidth >= 600.dp

        if (isWideLayout) {
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
          ) {
            Column(
              modifier = Modifier
                .weight(0.46f)
                .fillMaxHeight(),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              CalculatorTopBar(
                soundEnabled = prefs.soundEnabled,
                scientificVisible = prefs.scientificVisible,
                historyCount = uiState.history.size,
                onOpenEffects = { showEffectsSheet = true },
                onToggleScientific = viewModel::toggleScientificPanel,
                onToggleSound = viewModel::toggleSound,
                onOpenHistory = { showHistorySheet = true },
              )

              FxModePillBar(
                selectedMode = prefs.effectMode,
                onSelectMode = viewModel::setEffectMode,
              )

              CalculatorDisplay(
                expression = uiState.expression,
                mainText = uiState.displayMainText,
                previewResult = uiState.previewResult,
                isResultCommitted = uiState.isResultCommitted,
                errorMessage = uiState.errorMessage,
                errorTriggerId = uiState.errorTriggerId,
                resultTriggerId = uiState.resultTriggerId,
                isDegreeMode = prefs.isDegreeMode,
                scientificVisible = prefs.scientificVisible,
                onToggleDegreeMode = viewModel::toggleDegreeMode,
                onCenterChanged = viewModel::updateDisplayCenter,
                modifier = Modifier.weight(1f),
              )
            }

            Column(
              modifier = Modifier
                .weight(0.54f)
                .fillMaxHeight(),
              verticalArrangement = Arrangement.Bottom,
            ) {
              ScientificKeypadStrip(
                visible = prefs.scientificVisible,
                hapticsEnabled = prefs.hapticsEnabled,
                effectMode = prefs.effectMode,
                onScientificToken = viewModel::onScientificToken,
                onParenthesis = viewModel::onParenthesis,
              )

              CalculatorKeypad(
                hapticsEnabled = prefs.hapticsEnabled,
                effectMode = prefs.effectMode,
                isClearC = uiState.expression.isNotEmpty() && !uiState.isResultCommitted,
                onDigit = viewModel::onDigit,
                onDecimal = viewModel::onDecimal,
                onOperator = viewModel::onOperator,
                onPercent = viewModel::onPercent,
                onToggleSign = viewModel::onToggleSign,
                onBackspace = viewModel::onBackspace,
                onClearAll = viewModel::onClearAll,
                onEquals = viewModel::onEquals,
                modifier = Modifier.weight(1f),
              )
            }
          }
        } else {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .widthIn(max = 560.dp)
              .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            CalculatorTopBar(
              soundEnabled = prefs.soundEnabled,
              scientificVisible = prefs.scientificVisible,
              historyCount = uiState.history.size,
              onOpenEffects = { showEffectsSheet = true },
              onToggleScientific = viewModel::toggleScientificPanel,
              onToggleSound = viewModel::toggleSound,
              onOpenHistory = { showHistorySheet = true },
            )

            FxModePillBar(
              selectedMode = prefs.effectMode,
              onSelectMode = viewModel::setEffectMode,
            )

            CalculatorDisplay(
              expression = uiState.expression,
              mainText = uiState.displayMainText,
              previewResult = uiState.previewResult,
              isResultCommitted = uiState.isResultCommitted,
              errorMessage = uiState.errorMessage,
              errorTriggerId = uiState.errorTriggerId,
              resultTriggerId = uiState.resultTriggerId,
              isDegreeMode = prefs.isDegreeMode,
              scientificVisible = prefs.scientificVisible,
              onToggleDegreeMode = viewModel::toggleDegreeMode,
              onCenterChanged = viewModel::updateDisplayCenter,
              modifier = Modifier.weight(if (prefs.scientificVisible) 0.28f else 0.34f),
            )

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .weight(if (prefs.scientificVisible) 0.72f else 0.66f),
              verticalArrangement = Arrangement.Bottom,
            ) {
              ScientificKeypadStrip(
                visible = prefs.scientificVisible,
                hapticsEnabled = prefs.hapticsEnabled,
                effectMode = prefs.effectMode,
                onScientificToken = viewModel::onScientificToken,
                onParenthesis = viewModel::onParenthesis,
              )

              CalculatorKeypad(
                hapticsEnabled = prefs.hapticsEnabled,
                effectMode = prefs.effectMode,
                isClearC = uiState.expression.isNotEmpty() && !uiState.isResultCommitted,
                onDigit = viewModel::onDigit,
                onDecimal = viewModel::onDecimal,
                onOperator = viewModel::onOperator,
                onPercent = viewModel::onPercent,
                onToggleSign = viewModel::onToggleSign,
                onBackspace = viewModel::onBackspace,
                onClearAll = viewModel::onClearAll,
                onEquals = viewModel::onEquals,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }
    }

    FxOverlayCanvas(
      burstEvents = viewModel.burstEvents,
      effectMode = prefs.effectMode,
      intensity = prefs.effectIntensity,
      modifier = Modifier.fillMaxSize(),
    )
  }

  if (showEffectsSheet) {
    EffectsSettingsSheet(
      preferences = prefs,
      onSelectMode = viewModel::setEffectMode,
      onIntensityChanged = viewModel::setEffectIntensity,
      onToggleSound = viewModel::toggleSound,
      onToggleHaptics = viewModel::toggleHaptics,
      onToggleDarkTheme = viewModel::toggleDarkTheme,
      onToggleScientific = viewModel::toggleScientificPanel,
      onDismiss = { showEffectsSheet = false },
    )
  }

  if (showHistorySheet) {
    HistoryBottomSheet(
      history = uiState.history,
      onUseRecord = viewModel::onUseHistoryItem,
      onClearHistory = viewModel::onClearHistory,
      onDismiss = { showHistorySheet = false },
    )
  }
}

@Composable
private fun CalculatorTopBar(
  soundEnabled: Boolean,
  scientificVisible: Boolean,
  historyCount: Int,
  onOpenEffects: () -> Unit,
  onToggleScientific: () -> Unit,
  onToggleSound: () -> Unit,
  onOpenHistory: () -> Unit,
) {
  val fxColors = LocalFxColors.current

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(
      onClick = onOpenEffects,
      shape = RoundedCornerShape(16.dp),
      color = fxColors.accentPrimary.copy(alpha = if (fxColors.isDark) 0.16f else 0.12f),
      border = BorderStroke(1.dp, fxColors.accentPrimary.copy(alpha = 0.45f)),
      modifier = Modifier.testTag("open_effects_studio_button"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.AutoAwesome,
          contentDescription = "Открыть студию эффектов",
          tint = fxColors.accentPrimary,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = "Эффекты",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          color = fxColors.textPrimary,
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      IconButton(
        onClick = onToggleScientific,
        modifier = Modifier.testTag("toggle_scientific_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.Functions,
          contentDescription = "Инженерные функции",
          tint = if (scientificVisible) fxColors.accentPrimary else fxColors.textSecondary,
        )
      }

      IconButton(
        onClick = onToggleSound,
        modifier = Modifier.testTag("toggle_sound_button"),
      ) {
        Icon(
          imageVector = if (soundEnabled) {
            Icons.AutoMirrored.Rounded.VolumeUp
          } else {
            Icons.AutoMirrored.Rounded.VolumeOff
          },
          contentDescription = if (soundEnabled) "Выключить звук" else "Включить звук",
          tint = if (soundEnabled) fxColors.accentPrimary else fxColors.textSecondary,
        )
      }

      IconButton(
        onClick = onOpenHistory,
        modifier = Modifier.testTag("open_history_button"),
      ) {
        if (historyCount > 0) {
          BadgedBox(
            badge = {
              Badge(
                containerColor = fxColors.accentSecondary,
                contentColor = Color.White,
              ) {
                Text(historyCount.coerceAtMost(99).toString())
              }
            },
          ) {
            Icon(
              imageVector = Icons.Rounded.History,
              contentDescription = "История вычислений",
              tint = fxColors.textPrimary,
            )
          }
        } else {
          Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = "История вычислений",
            tint = fxColors.textSecondary,
          )
        }
      }
    }
  }
}
