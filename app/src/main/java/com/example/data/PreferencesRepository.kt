package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fx.EffectMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fx_calc_prefs")

data class FxPreferences(
  val effectMode: EffectMode = EffectMode.NEON,
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val darkTheme: Boolean = true,
  val scientificVisible: Boolean = false,
  val isDegreeMode: Boolean = true,
  val effectIntensity: Float = 1.0f,
)

class PreferencesRepository(private val context: Context) {

  private object Keys {
    val EFFECT_MODE = stringPreferencesKey("effect_mode")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    val DARK_THEME = booleanPreferencesKey("dark_theme")
    val SCIENTIFIC_VISIBLE = booleanPreferencesKey("scientific_visible")
    val DEGREE_MODE = booleanPreferencesKey("degree_mode")
    val EFFECT_INTENSITY = floatPreferencesKey("effect_intensity")
  }

  val preferencesFlow: Flow<FxPreferences> = context.dataStore.data.map { prefs ->
    FxPreferences(
      effectMode = EffectMode.fromKey(prefs[Keys.EFFECT_MODE]),
      soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
      hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
      darkTheme = prefs[Keys.DARK_THEME] ?: true,
      scientificVisible = prefs[Keys.SCIENTIFIC_VISIBLE] ?: false,
      isDegreeMode = prefs[Keys.DEGREE_MODE] ?: true,
      effectIntensity = (prefs[Keys.EFFECT_INTENSITY] ?: 1.0f).coerceIn(0.4f, 1.5f),
    )
  }

  suspend fun setEffectMode(mode: EffectMode) {
    context.dataStore.edit { it[Keys.EFFECT_MODE] = mode.storageKey }
  }

  suspend fun setSoundEnabled(enabled: Boolean) {
    context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
  }

  suspend fun setHapticsEnabled(enabled: Boolean) {
    context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
  }

  suspend fun setDarkTheme(dark: Boolean) {
    context.dataStore.edit { it[Keys.DARK_THEME] = dark }
  }

  suspend fun setScientificVisible(visible: Boolean) {
    context.dataStore.edit { it[Keys.SCIENTIFIC_VISIBLE] = visible }
  }

  suspend fun setDegreeMode(isDegree: Boolean) {
    context.dataStore.edit { it[Keys.DEGREE_MODE] = isDegree }
  }

  suspend fun setEffectIntensity(intensity: Float) {
    context.dataStore.edit { it[Keys.EFFECT_INTENSITY] = intensity.coerceIn(0.4f, 1.5f) }
  }
}
