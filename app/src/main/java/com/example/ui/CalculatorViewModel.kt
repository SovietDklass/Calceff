package com.example.ui

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.CalculatorEngine
import com.example.calculator.EvaluationResult
import com.example.data.FxPreferences
import com.example.data.PreferencesRepository
import com.example.fx.EffectMode
import com.example.fx.FxBurstEvent
import com.example.fx.FxBurstKind
import com.example.fx.SoundFxPlayer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculationRecord(
  val id: Long,
  val expression: String,
  val result: String,
)

data class CalculatorUiState(
  val expression: String = "",
  val previewResult: String = "",
  val displayMainText: String = "0",
  val isResultCommitted: Boolean = false,
  val errorMessage: String? = null,
  val errorTriggerId: Long = 0L,
  val resultTriggerId: Long = 0L,
  val history: List<CalculationRecord> = emptyList(),
  val preferences: FxPreferences = FxPreferences(),
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

  private val preferencesRepository = PreferencesRepository(application.applicationContext)
  private val soundPlayer = SoundFxPlayer()

  private val _uiState = MutableStateFlow(CalculatorUiState())
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  private val _burstEvents = MutableSharedFlow<FxBurstEvent>(extraBufferCapacity = 32)
  val burstEvents: SharedFlow<FxBurstEvent> = _burstEvents.asSharedFlow()

  private var nextEventId = 1L
  private var displayCenter = Offset(540f, 360f)

  init {
    viewModelScope.launch {
      preferencesRepository.preferencesFlow.collect { prefs ->
        _uiState.update { state ->
          val updated = state.copy(preferences = prefs)
          recomputePreview(updated)
        }
      }
    }
  }

  fun updateDisplayCenter(center: Offset) {
    displayCenter = center
  }

  fun onDigit(digit: String, origin: Offset) {
    emitBurst(FxBurstKind.DIGIT_TAP, origin, digit)
    _uiState.update { state ->
      val baseExpr = if (state.isResultCommitted) "" else state.expression
      val newExpr = when {
        baseExpr == "0" && digit != "." -> digit
        else -> baseExpr + digit
      }
      recomputePreview(
        state.copy(
          expression = newExpr,
          isResultCommitted = false,
          errorMessage = null,
        ),
      )
    }
  }

  fun onDecimal(origin: Offset) {
    emitBurst(FxBurstKind.DIGIT_TAP, origin, ".")
    _uiState.update { state ->
      val baseExpr = if (state.isResultCommitted) "0" else state.expression
      val lastNumberSegment = baseExpr.takeLastWhile { it.isDigit() || it == '.' }
      if (lastNumberSegment.contains('.')) {
        return@update state
      }
      val append = if (baseExpr.isEmpty() || !baseExpr.last().isDigit()) "0." else "."
      recomputePreview(
        state.copy(
          expression = baseExpr + append,
          isResultCommitted = false,
          errorMessage = null,
        ),
      )
    }
  }

  fun onOperator(op: String, origin: Offset) {
    emitBurst(FxBurstKind.OPERATOR_TAP, origin, op)
    _uiState.update { state ->
      var expr = state.expression
      if (state.isResultCommitted) {
        expr = state.displayMainText
      }
      if (expr.isEmpty()) {
        if (op == "−" || op == "-") {
          expr = "−"
        } else {
          return@update state
        }
      } else {
        val last = expr.last()
        if (isBinaryOperator(last)) {
          if ((op == "−" || op == "-") && (last == '×' || last == '÷' || last == '^')) {
            expr += "−"
          } else if (expr.length >= 2 && isBinaryOperator(expr[expr.length - 2]) && last == '−') {
            expr = expr.dropLast(2) + op
          } else {
            expr = expr.dropLast(1) + op
          }
        } else if (last == '(' && (op != "−" && op != "-")) {
          return@update state
        } else {
          expr += op
        }
      }
      recomputePreview(
        state.copy(
          expression = expr,
          isResultCommitted = false,
          errorMessage = null,
        ),
      )
    }
  }

  fun onPercent(origin: Offset) {
    emitBurst(FxBurstKind.OPERATOR_TAP, origin, "%")
    _uiState.update { state ->
      val expr = if (state.isResultCommitted) state.displayMainText else state.expression
      if (expr.isEmpty()) return@update state
      val last = expr.last()
      if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
        recomputePreview(
          state.copy(
            expression = "$expr%",
            isResultCommitted = false,
            errorMessage = null,
          ),
        )
      } else {
        state
      }
    }
  }

  fun onToggleSign(origin: Offset) {
    emitBurst(FxBurstKind.OPERATOR_TAP, origin, "±")
    _uiState.update { state ->
      val expr = if (state.isResultCommitted) state.displayMainText else state.expression
      if (expr.isEmpty()) {
        return@update recomputePreview(
          state.copy(expression = "−", isResultCommitted = false, errorMessage = null),
        )
      }
      val newExpr = if (expr.startsWith("−") && expr.drop(1).all { it.isDigit() || it == '.' }) {
        expr.drop(1)
      } else if (expr.all { it.isDigit() || it == '.' }) {
        "−$expr"
      } else if (expr.endsWith("(−")) {
        expr.dropLast(2)
      } else {
        val tailDigits = expr.takeLastWhile { it.isDigit() || it == '.' }
        if (tailDigits.isNotEmpty()) {
          val prefix = expr.dropLast(tailDigits.length)
          if (prefix.endsWith("(−")) {
            prefix.dropLast(2) + tailDigits
          } else {
            "$prefix(−$tailDigits)"
          }
        } else {
          "$expr(−"
        }
      }
      recomputePreview(
        state.copy(expression = newExpr, isResultCommitted = false, errorMessage = null),
      )
    }
  }

  fun onScientificToken(token: String, origin: Offset) {
    emitBurst(FxBurstKind.OPERATOR_TAP, origin, token)
    _uiState.update { state ->
      val base = if (state.isResultCommitted) {
        when (token) {
          "x²" -> "(${state.displayMainText})^2"
          "!" -> "(${state.displayMainText})!"
          "^" -> "${state.displayMainText}^"
          "√(" -> "√(${state.displayMainText})"
          "sin(", "cos(", "tan(", "ln(" -> "$token${state.displayMainText})"
          else -> token
        }
      } else {
        when (token) {
          "x²" -> if (state.expression.isEmpty()) "" else "${state.expression}^2"
          "!" -> if (state.expression.isEmpty()) "" else "${state.expression}!"
          "^" -> if (state.expression.isEmpty()) "" else "${state.expression}^"
          else -> state.expression + token
        }
      }
      recomputePreview(
        state.copy(expression = base, isResultCommitted = false, errorMessage = null),
      )
    }
  }

  fun onParenthesis(paren: String, origin: Offset) {
    emitBurst(FxBurstKind.OPERATOR_TAP, origin, paren)
    _uiState.update { state ->
      val base = if (state.isResultCommitted) "" else state.expression
      recomputePreview(
        state.copy(expression = base + paren, isResultCommitted = false, errorMessage = null),
      )
    }
  }

  fun onBackspace(origin: Offset) {
    emitBurst(FxBurstKind.CLEAR_SWEEP, origin, "⌫")
    _uiState.update { state ->
      if (state.isResultCommitted) {
        return@update state.copy(
          expression = "",
          previewResult = "",
          displayMainText = "0",
          isResultCommitted = false,
          errorMessage = null,
        )
      }
      if (state.expression.isEmpty()) return@update state

      val trimmed = when {
        state.expression.endsWith("sin(") -> state.expression.dropLast(4)
        state.expression.endsWith("cos(") -> state.expression.dropLast(4)
        state.expression.endsWith("tan(") -> state.expression.dropLast(4)
        state.expression.endsWith("ln(") -> state.expression.dropLast(3)
        state.expression.endsWith("√(") -> state.expression.dropLast(2)
        else -> state.expression.dropLast(1)
      }
      recomputePreview(
        state.copy(expression = trimmed, errorMessage = null),
      )
    }
  }

  fun onClearAll(origin: Offset) {
    emitBurst(FxBurstKind.CLEAR_SWEEP, origin, "AC")
    _uiState.update { state ->
      state.copy(
        expression = "",
        previewResult = "",
        displayMainText = "0",
        isResultCommitted = false,
        errorMessage = null,
      )
    }
  }

  fun onEquals(origin: Offset) {
    val current = _uiState.value
    if (current.expression.isBlank()) {
      emitBurst(FxBurstKind.DIGIT_TAP, origin, "=")
      return
    }
    if (current.isResultCommitted) {
      emitBurst(FxBurstKind.EQUALS_CELEBRATION, displayCenter, "=")
      _uiState.update { it.copy(resultTriggerId = it.resultTriggerId + 1L) }
      return
    }

    val balanced = CalculatorEngine.balanceParentheses(current.expression)
    when (val eval = CalculatorEngine.evaluate(balanced, current.preferences.isDegreeMode)) {
      is EvaluationResult.Success -> {
        emitBurst(FxBurstKind.EQUALS_CELEBRATION, origin, "=")
        emitBurst(FxBurstKind.EQUALS_CELEBRATION, displayCenter, eval.formatted)
        val record = CalculationRecord(
          id = nextEventId++,
          expression = balanced,
          result = eval.formatted,
        )
        _uiState.update { state ->
          state.copy(
            expression = balanced,
            previewResult = "",
            displayMainText = eval.formatted,
            isResultCommitted = true,
            errorMessage = null,
            resultTriggerId = state.resultTriggerId + 1L,
            history = (listOf(record) + state.history).take(50),
          )
        }
      }
      is EvaluationResult.Error -> {
        emitBurst(FxBurstKind.ERROR_GLITCH, displayCenter, "!")
        _uiState.update { state ->
          state.copy(
            errorMessage = eval.messageRu,
            errorTriggerId = state.errorTriggerId + 1L,
          )
        }
      }
    }
  }

  fun onUseHistoryItem(record: CalculationRecord, useExpression: Boolean) {
    emitBurst(FxBurstKind.OPERATOR_TAP, displayCenter, record.result)
    _uiState.update { state ->
      if (useExpression) {
        recomputePreview(
          state.copy(
            expression = record.expression,
            isResultCommitted = false,
            errorMessage = null,
          ),
        )
      } else {
        recomputePreview(
          state.copy(
            expression = record.result,
            isResultCommitted = false,
            errorMessage = null,
          ),
        )
      }
    }
  }

  fun onClearHistory() {
    _uiState.update { it.copy(history = emptyList()) }
  }

  fun setEffectMode(mode: EffectMode) {
    viewModelScope.launch {
      preferencesRepository.setEffectMode(mode)
      emitBurst(FxBurstKind.EQUALS_CELEBRATION, displayCenter, mode.titleRu)
    }
  }

  fun toggleSound() {
    val next = !_uiState.value.preferences.soundEnabled
    viewModelScope.launch { preferencesRepository.setSoundEnabled(next) }
  }

  fun toggleHaptics() {
    val next = !_uiState.value.preferences.hapticsEnabled
    viewModelScope.launch { preferencesRepository.setHapticsEnabled(next) }
  }

  fun toggleDarkTheme() {
    val next = !_uiState.value.preferences.darkTheme
    viewModelScope.launch { preferencesRepository.setDarkTheme(next) }
  }

  fun toggleScientificPanel() {
    val next = !_uiState.value.preferences.scientificVisible
    viewModelScope.launch { preferencesRepository.setScientificVisible(next) }
  }

  fun toggleDegreeMode() {
    val next = !_uiState.value.preferences.isDegreeMode
    viewModelScope.launch { preferencesRepository.setDegreeMode(next) }
  }

  fun setEffectIntensity(value: Float) {
    viewModelScope.launch { preferencesRepository.setEffectIntensity(value) }
  }

  private fun recomputePreview(state: CalculatorUiState): CalculatorUiState {
    if (state.expression.isBlank()) {
      return state.copy(displayMainText = "0", previewResult = "")
    }
    if (state.isResultCommitted) {
      return state
    }
    val hasOperatorOrFunc = state.expression.any {
      it in charArrayOf('+', '−', '-', '×', '÷', '%', '^', '!', '√', 'π', 'e', '(') || it.isLetter()
    }
    if (!hasOperatorOrFunc) {
      return state.copy(displayMainText = state.expression, previewResult = "")
    }
    val preview = when (val eval = CalculatorEngine.evaluate(state.expression, state.preferences.isDegreeMode)) {
      is EvaluationResult.Success -> eval.formatted
      is EvaluationResult.Error -> ""
    }
    return state.copy(
      displayMainText = state.expression,
      previewResult = if (preview == state.expression) "" else preview,
    )
  }

  private fun emitBurst(kind: FxBurstKind, origin: Offset, label: String) {
    val prefs = _uiState.value.preferences
    if (prefs.soundEnabled) {
      soundPlayer.play(kind = kind, label = label, mode = prefs.effectMode)
    }
    _burstEvents.tryEmit(
      FxBurstEvent(
        id = nextEventId++,
        kind = kind,
        originX = origin.x,
        originY = origin.y,
        label = label,
      ),
    )
  }

  private fun isBinaryOperator(ch: Char): Boolean =
    ch == '+' || ch == '−' || ch == '-' || ch == '×' || ch == '÷' || ch == '^'

  override fun onCleared() {
    super.onCleared()
    soundPlayer.release()
  }
}
