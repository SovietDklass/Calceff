package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

sealed interface EvaluationResult {
  data class Success(val value: Double, val formatted: String) : EvaluationResult
  data class Error(val messageRu: String) : EvaluationResult
}

object CalculatorEngine {

  private val bigMathContext = MathContext(16, RoundingMode.HALF_UP)

  fun evaluate(expression: String, isDegreeMode: Boolean): EvaluationResult {
    val normalized = expression.trim()
    if (normalized.isEmpty()) return EvaluationResult.Success(0.0, "0")

    return try {
      val autoClosed = balanceParentheses(normalized)
      val tokens = tokenize(autoClosed)
      if (tokens.isEmpty()) return EvaluationResult.Success(0.0, "0")

      val parser = Parser(tokens, isDegreeMode)
      val result = parser.parseExpression()
      if (parser.hasMore()) {
        return EvaluationResult.Error("Ошибка синтаксиса")
      }
      if (result.isNaN()) {
        return EvaluationResult.Error("Не определено")
      }
      if (result.isInfinite()) {
        return EvaluationResult.Error("Бесконечность")
      }

      EvaluationResult.Success(result, formatNumber(result))
    } catch (_: DivisionByZeroException) {
      EvaluationResult.Error("Деление на ноль")
    } catch (e: MathDomainException) {
      EvaluationResult.Error(e.message ?: "Недопустимая операция")
    } catch (_: Exception) {
      EvaluationResult.Error("Ошибка выражения")
    }
  }

  fun balanceParentheses(expr: String): String {
    var openCount = 0
    for (ch in expr) {
      if (ch == '(') openCount++
      else if (ch == ')' && openCount > 0) openCount--
    }
    if (openCount <= 0) return expr
    return expr + ")".repeat(openCount)
  }

  fun formatNumber(value: Double): String {
    if (value == 0.0) return "0"
    val cleaned = if (kotlin.math.abs(value) < 1e-13) 0.0 else value
    if (cleaned == 0.0) return "0"

    val absVal = kotlin.math.abs(cleaned)
    if (absVal >= 1e12 || (absVal < 1e-7 && absVal > 0.0)) {
      val formattedSci = String.format(java.util.Locale.US, "%.6e", cleaned)
      return formattedSci.replace(Regex("\\.?0+e"), "e")
    }

    val bd = try {
      BigDecimal.valueOf(cleaned)
        .round(MathContext(12, RoundingMode.HALF_UP))
        .stripTrailingZeros()
    } catch (_: NumberFormatException) {
      return cleaned.toString()
    }
    val plain = bd.toPlainString()
    return if (plain == "-0") "0" else plain
  }

  private sealed interface Token {
    data class NumberToken(val value: Double) : Token
    data class OperatorToken(val op: Char) : Token
    data class FunctionToken(val name: String) : Token
    data object LeftParen : Token
    data object RightParen : Token
    data object Percent : Token
    data object Factorial : Token
  }

  private fun tokenize(input: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    val n = input.length

    while (i < n) {
      val ch = input[i]
      when {
        ch.isWhitespace() -> i++
        ch.isDigit() || ch == '.' -> {
          val start = i
          var hasDot = ch == '.'
          i++
          while (i < n) {
            val c = input[i]
            if (c.isDigit()) {
              i++
            } else if (c == '.' && !hasDot) {
              hasDot = true
              i++
            } else if ((c == 'e' || c == 'E') && i + 1 < n) {
              val next = input[i + 1]
              if (next.isDigit() || ((next == '+' || next == '-') && i + 2 < n && input[i + 2].isDigit())) {
                i++
                if (input[i] == '+' || input[i] == '-') i++
              } else {
                break
              }
            } else {
              break
            }
          }
          val numStr = input.substring(start, i)
          val num = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
          insertImplicitMultiplicationIfNeeded(tokens)
          tokens.add(Token.NumberToken(num))
        }
        ch == 'π' -> {
          insertImplicitMultiplicationIfNeeded(tokens)
          tokens.add(Token.NumberToken(PI))
          i++
        }
        ch == 'e' -> {
          insertImplicitMultiplicationIfNeeded(tokens)
          tokens.add(Token.NumberToken(exp(1.0)))
          i++
        }
        ch.isLetter() -> {
          val start = i
          while (i < n && input[i].isLetter()) i++
          val name = input.substring(start, i)
          insertImplicitMultiplicationIfNeeded(tokens)
          tokens.add(Token.FunctionToken(name))
        }
        ch == '√' -> {
          insertImplicitMultiplicationIfNeeded(tokens)
          tokens.add(Token.FunctionToken("sqrt"))
          i++
        }
        ch == '(' -> {
          insertImplicitMultiplicationIfNeeded(tokens)
          tokens.add(Token.LeftParen)
          i++
        }
        ch == ')' -> {
          tokens.add(Token.RightParen)
          i++
        }
        ch == '%' -> {
          tokens.add(Token.Percent)
          i++
        }
        ch == '!' -> {
          tokens.add(Token.Factorial)
          i++
        }
        ch == '+' || ch == '-' || ch == '−' || ch == '*' || ch == '×' || ch == '/' || ch == '÷' || ch == '^' -> {
          val normalizedOp = when (ch) {
            '−' -> '-'
            '×' -> '*'
            '÷' -> '/'
            else -> ch
          }
          tokens.add(Token.OperatorToken(normalizedOp))
          i++
        }
        else -> throw IllegalArgumentException("Unknown char: $ch")
      }
    }
    return tokens
  }

  private fun insertImplicitMultiplicationIfNeeded(tokens: MutableList<Token>) {
    val prev = tokens.lastOrNull() ?: return
    if (prev is Token.NumberToken || prev is Token.RightParen || prev is Token.Percent || prev is Token.Factorial) {
      tokens.add(Token.OperatorToken('*'))
    }
  }

  private class DivisionByZeroException : RuntimeException()
  private class MathDomainException(message: String) : RuntimeException(message)

  private class Parser(
    private val tokens: List<Token>,
    private val isDegreeMode: Boolean,
  ) {
    private var pos = 0

    fun hasMore(): Boolean = pos < tokens.size

    private fun peek(): Token? = tokens.getOrNull(pos)

    private fun consume(): Token = tokens[pos++]

    fun parseExpression(): Double {
      var left = parseTerm()
      while (true) {
        val opToken = peek() as? Token.OperatorToken ?: break
        if (opToken.op != '+' && opToken.op != '-') break
        consume()

        val rightStartPos = pos
        val rightResult = parseTermWithPercentInfo()
        val adjustedRight = if (rightResult.isStandalonePercent) {
          left * rightResult.value
        } else {
          rightResult.value
        }

        left = if (opToken.op == '+') {
          safeAdd(left, adjustedRight)
        } else {
          safeSubtract(left, adjustedRight)
        }
        if (rightStartPos == pos) break
      }
      return left
    }

    private data class TermValue(val value: Double, val isStandalonePercent: Boolean)

    private fun parseTermWithPercentInfo(): TermValue {
      val factorStart = pos
      val factorValue = parseFactor()
      val next = peek()
      val isSimplePercent = next !is Token.OperatorToken || (next.op != '*' && next.op != '/')
      val endedWithPercent = pos > factorStart && tokens[pos - 1] is Token.Percent
      if (isSimplePercent && endedWithPercent) {
        return TermValue(factorValue, isStandalonePercent = true)
      }

      var left = factorValue
      while (true) {
        val opToken = peek() as? Token.OperatorToken ?: break
        if (opToken.op != '*' && opToken.op != '/') break
        consume()
        val right = parseFactor()
        left = if (opToken.op == '*') {
          safeMultiply(left, right)
        } else {
          if (right == 0.0) throw DivisionByZeroException()
          safeDivide(left, right)
        }
      }
      return TermValue(left, isStandalonePercent = false)
    }

    private fun parseTerm(): Double {
      var left = parseFactor()
      while (true) {
        val opToken = peek() as? Token.OperatorToken ?: break
        if (opToken.op != '*' && opToken.op != '/') break
        consume()
        val right = parseFactor()
        left = if (opToken.op == '*') {
          safeMultiply(left, right)
        } else {
          if (right == 0.0) throw DivisionByZeroException()
          safeDivide(left, right)
        }
      }
      return left
    }

    private fun parseFactor(): Double {
      val base = parseUnary()
      val opToken = peek() as? Token.OperatorToken
      if (opToken != null && opToken.op == '^') {
        consume()
        val exponent = parseFactor()
        val res = base.pow(exponent)
        if (res.isNaN()) throw MathDomainException("Недопустимая степень")
        return res
      }
      return base
    }

    private fun parseUnary(): Double {
      val token = peek()
      if (token is Token.OperatorToken && (token.op == '+' || token.op == '-')) {
        consume()
        val inner = parseUnary()
        return if (token.op == '-') -inner else inner
      }
      return parsePostfix()
    }

    private fun parsePostfix(): Double {
      var value = parsePrimary()
      while (true) {
        when (peek()) {
          is Token.Percent -> {
            consume()
            value /= 100.0
          }
          is Token.Factorial -> {
            consume()
            value = factorial(value)
          }
          else -> break
        }
      }
      return value
    }

    private fun parsePrimary(): Double {
      return when (val token = peek()) {
        is Token.NumberToken -> {
          consume()
          token.value
        }
        is Token.LeftParen -> {
          consume()
          val value = parseExpression()
          if (peek() is Token.RightParen) {
            consume()
          }
          value
        }
        is Token.FunctionToken -> {
          consume()
          val arg = if (peek() is Token.LeftParen) {
            consume()
            val inner = parseExpression()
            if (peek() is Token.RightParen) consume()
            inner
          } else {
            parseUnary()
          }
          applyFunction(token.name, arg)
        }
        else -> throw IllegalArgumentException("Unexpected token: $token")
      }
    }

    private fun applyFunction(name: String, arg: Double): Double {
      val angleRad = if (isDegreeMode) Math.toRadians(arg) else arg
      return when (name.lowercase()) {
        "sin" -> {
          val v = sin(angleRad)
          if (kotlin.math.abs(v) < 1e-15) 0.0 else v
        }
        "cos" -> {
          val v = cos(angleRad)
          if (kotlin.math.abs(v) < 1e-15) 0.0 else v
        }
        "tan" -> {
          val c = cos(angleRad)
          if (kotlin.math.abs(c) < 1e-14) throw MathDomainException("Тангенс не определён")
          val v = tan(angleRad)
          if (kotlin.math.abs(v) < 1e-15) 0.0 else v
        }
        "sqrt" -> {
          if (arg < 0.0) throw MathDomainException("Корень из отрицательного")
          sqrt(arg)
        }
        "ln" -> {
          if (arg <= 0.0) throw MathDomainException("Логарифм ≤ 0")
          ln(arg)
        }
        "log" -> {
          if (arg <= 0.0) throw MathDomainException("Логарифм ≤ 0")
          log10(arg)
        }
        else -> throw IllegalArgumentException("Unknown function: $name")
      }
    }

    private fun factorial(value: Double): Double {
      if (value < 0.0 || value != kotlin.math.floor(value)) {
        throw MathDomainException("Факториал только для целых ≥ 0")
      }
      if (value > 170.0) throw MathDomainException("Слишком большое число")
      var res = 1.0
      for (k in 2..value.toInt()) {
        res *= k
      }
      return res
    }

    private fun safeAdd(a: Double, b: Double): Double =
      try {
        BigDecimal.valueOf(a).add(BigDecimal.valueOf(b), bigMathContext).toDouble()
      } catch (_: Exception) {
        a + b
      }

    private fun safeSubtract(a: Double, b: Double): Double =
      try {
        BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b), bigMathContext).toDouble()
      } catch (_: Exception) {
        a - b
      }

    private fun safeMultiply(a: Double, b: Double): Double =
      try {
        BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b), bigMathContext).toDouble()
      } catch (_: Exception) {
        a * b
      }

    private fun safeDivide(a: Double, b: Double): Double =
      try {
        BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), bigMathContext).toDouble()
      } catch (_: Exception) {
        a / b
      }
  }
}
