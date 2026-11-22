package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FxPreferences
import com.example.fx.EffectMode
import com.example.ui.CalculationRecord
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalFxColors
import kotlin.math.roundToInt

@Composable
fun FxModePillBar(
  selectedMode: EffectMode,
  onSelectMode: (EffectMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  val fxColors = LocalFxColors.current
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(scrollState),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    EffectMode.entries.forEach { mode ->
      val isSelected = mode == selectedMode
      val modeColor = mode.primaryColor(fxColors.isDark)
      val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
          modeColor.copy(alpha = if (fxColors.isDark) 0.22f else 0.16f)
        } else {
          fxColors.keyDigitContainer.copy(alpha = if (fxColors.isDark) 0.72f else 0.85f)
        },
        label = "pillBg",
      )
      val borderColor by animateColorAsState(
        targetValue = if (isSelected) modeColor else fxColors.keyBorder.copy(alpha = 0.55f),
        label = "pillBorder",
      )

      Surface(
        onClick = { onSelectMode(mode) },
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.4.dp else 1.dp, borderColor),
        modifier = Modifier
          .height(40.dp)
          .testTag("pill_mode_${mode.storageKey}"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = modeIcon(mode),
            contentDescription = mode.titleRu,
            tint = if (isSelected) modeColor else fxColors.textSecondary,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = mode.titleRu,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 13.sp,
            ),
            color = if (isSelected) fxColors.textPrimary else fxColors.textSecondary,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsSettingsSheet(
  preferences: FxPreferences,
  onSelectMode: (EffectMode) -> Unit,
  onIntensityChanged: (Float) -> Unit,
  onToggleSound: () -> Unit,
  onToggleHaptics: () -> Unit,
  onToggleDarkTheme: () -> Unit,
  onToggleScientific: () -> Unit,
  onDismiss: () -> Unit,
) {
  val fxColors = LocalFxColors.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = fxColors.textPrimary,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = "Студия эффектов",
        style = MaterialTheme.typography.headlineSmall,
        color = fxColors.textPrimary,
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Визуальный режим",
          style = MaterialTheme.typography.titleMedium,
          color = fxColors.textSecondary,
        )

        EffectMode.entries.forEach { mode ->
          val selected = mode == preferences.effectMode
          val accent = mode.primaryColor(fxColors.isDark)
          Surface(
            onClick = { onSelectMode(mode) },
            shape = RoundedCornerShape(18.dp),
            color = if (selected) {
              accent.copy(alpha = if (fxColors.isDark) 0.16f else 0.12f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            },
            border = BorderStroke(
              width = if (selected) 1.5.dp else 1.dp,
              color = if (selected) accent else fxColors.keyBorder.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("sheet_mode_${mode.storageKey}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
              ) {
                Surface(
                  shape = CircleShape,
                  color = accent.copy(alpha = 0.18f),
                  modifier = Modifier.size(38.dp),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = modeIcon(mode),
                      contentDescription = mode.titleRu,
                      tint = accent,
                      modifier = Modifier.size(20.dp),
                    )
                  }
                }
                Column {
                  Text(
                    text = mode.titleRu,
                    style = MaterialTheme.typography.titleMedium,
                    color = fxColors.textPrimary,
                  )
                  Text(
                    text = mode.subtitleRu,
                    style = MaterialTheme.typography.bodyMedium,
                    color = fxColors.textSecondary,
                  )
                }
              }

              if (selected) {
                Icon(
                  imageVector = Icons.Rounded.Check,
                  contentDescription = "Выбрано",
                  tint = accent,
                  modifier = Modifier.size(22.dp),
                )
              }
            }
          }
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Интенсивность частиц",
            style = MaterialTheme.typography.titleMedium,
            color = fxColors.textPrimary,
          )
          Text(
            text = "${(preferences.effectIntensity * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
            ),
            color = fxColors.accentPrimary,
          )
        }
        Slider(
          value = preferences.effectIntensity,
          onValueChange = onIntensityChanged,
          valueRange = 0.4f..1.5f,
          colors = SliderDefaults.colors(
            thumbColor = fxColors.accentPrimary,
            activeTrackColor = fxColors.accentPrimary,
            inactiveTrackColor = fxColors.keyBorder,
          ),
          modifier = Modifier.testTag("intensity_slider"),
        )
      }

      HorizontalDivider(color = fxColors.keyBorder.copy(alpha = 0.45f))

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FxToggleRow(
          icon = Icons.AutoMirrored.Rounded.VolumeUp,
          title = "Звук клавиш (Синтезатор)",
          subtitle = "Музыкальные тоны при каждом нажатии",
          checked = preferences.soundEnabled,
          onToggle = onToggleSound,
          testTagName = "switch_sound",
        )
        FxToggleRow(
          icon = Icons.Rounded.Vibration,
          title = "Тактильный отклик",
          subtitle = "Лёгкая вибрация клавиш и результата",
          checked = preferences.hapticsEnabled,
          onToggle = onToggleHaptics,
          testTagName = "switch_haptics",
        )
        FxToggleRow(
          icon = Icons.Rounded.DarkMode,
          title = "Тёмная тема",
          subtitle = "Контрастная неоновая палитра",
          checked = preferences.darkTheme,
          onToggle = onToggleDarkTheme,
          testTagName = "switch_dark_theme",
        )
        FxToggleRow(
          icon = Icons.Rounded.Functions,
          title = "Инженерные функции",
          subtitle = "sin, cos, tan, √, x², ^, ln, π, e, !",
          checked = preferences.scientificVisible,
          onToggle = onToggleScientific,
          testTagName = "switch_scientific",
        )
      }
    }
  }
}

@Composable
private fun FxToggleRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onToggle: () -> Unit,
  testTagName: String,
) {
  val fxColors = LocalFxColors.current
  Surface(
    onClick = onToggle,
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTagName),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = if (checked) fxColors.accentPrimary else fxColors.textSecondary,
          modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.padding(end = 8.dp)) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
            color = fxColors.textPrimary,
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            color = fxColors.textSecondary,
          )
        }
      }
      Switch(
        checked = checked,
        onCheckedChange = { onToggle() },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = fxColors.accentPrimary,
        ),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
  history: List<CalculationRecord>,
  onUseRecord: (CalculationRecord, Boolean) -> Unit,
  onClearHistory: () -> Unit,
  onDismiss: () -> Unit,
) {
  val fxColors = LocalFxColors.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = fxColors.textPrimary,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "История вычислений",
          style = MaterialTheme.typography.headlineSmall,
          color = fxColors.textPrimary,
        )
        if (history.isNotEmpty()) {
          TextButton(
            onClick = onClearHistory,
            modifier = Modifier.testTag("clear_history_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.DeleteSweep,
              contentDescription = "Очистить историю",
              tint = fxColors.accentSecondary,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
              text = "Очистить",
              color = fxColors.accentSecondary,
            )
          }
        }
      }

      if (history.isEmpty()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
            .testTag("history_empty_state"),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Surface(
            shape = CircleShape,
            color = fxColors.accentPrimary.copy(alpha = 0.14f),
            modifier = Modifier.size(56.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = "История пуста",
                tint = fxColors.accentPrimary,
                modifier = Modifier.size(28.dp),
              )
            }
          }
          Text(
            text = "Пока пусто",
            style = MaterialTheme.typography.titleMedium,
            color = fxColors.textPrimary,
          )
          Text(
            text = "Ваши расчёты появятся здесь сразу после нажатия «=»",
            style = MaterialTheme.typography.bodyMedium,
            color = fxColors.textSecondary,
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .testTag("history_list"),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(history, key = { it.id }) { record ->
            Surface(
              onClick = {
                onUseRecord(record, false)
                onDismiss()
              },
              shape = RoundedCornerShape(18.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
              border = BorderStroke(1.dp, fxColors.keyBorder.copy(alpha = 0.45f)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("history_item_${record.id}"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = record.expression,
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 15.sp,
                  ),
                  color = fxColors.textSecondary,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "= ${record.result}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                      fontFamily = JetBrainsMonoFontFamily,
                      fontSize = 22.sp,
                    ),
                    color = fxColors.accentPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                  )
                  TextButton(
                    onClick = {
                      onUseRecord(record, true)
                      onDismiss()
                    },
                    modifier = Modifier.testTag("history_use_expr_${record.id}"),
                  ) {
                    Text(
                      text = "Выражение",
                      style = MaterialTheme.typography.labelMedium,
                      color = fxColors.textSecondary,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun modeIcon(mode: EffectMode): ImageVector = when (mode) {
  EffectMode.NEON -> Icons.Rounded.AutoAwesome
  EffectMode.SPARKS -> Icons.Rounded.Bolt
  EffectMode.RIPPLE -> Icons.Rounded.WaterDrop
  EffectMode.MATRIX -> Icons.Rounded.Code
  EffectMode.CONFETTI -> Icons.Rounded.Celebration
}
