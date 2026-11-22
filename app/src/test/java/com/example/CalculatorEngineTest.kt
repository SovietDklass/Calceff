package com.example

import com.example.calculator.CalculatorEngine
import com.example.calculator.EvaluationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

  @Test
  fun basicArithmeticAndPrecedence() {
    val res = CalculatorEngine.evaluate("2+3×4−6÷2", isDegreeMode = true)
    assertTrue(res is EvaluationResult.Success)
    assertEquals("11", (res as EvaluationResult.Success).formatted)
  }

  @Test
  fun decimalPrecisionAvoidsFloatingPointArtifacts() {
    val res = CalculatorEngine.evaluate("0.1+0.2", isDegreeMode = true)
    assertTrue(res is EvaluationResult.Success)
    assertEquals("0.3", (res as EvaluationResult.Success).formatted)
  }

  @Test
  fun smartPercentageInAdditionAndSubtraction() {
    val add = CalculatorEngine.evaluate("200+10%", isDegreeMode = true)
    assertTrue(add is EvaluationResult.Success)
    assertEquals("220", (add as EvaluationResult.Success).formatted)

    val sub = CalculatorEngine.evaluate("200−25%", isDegreeMode = true)
    assertTrue(sub is EvaluationResult.Success)
    assertEquals("150", (sub as EvaluationResult.Success).formatted)
  }

  @Test
  fun scientificFunctionsAndParentheses() {
    val trig = CalculatorEngine.evaluate("sin(30)+cos(60)", isDegreeMode = true)
    assertTrue(trig is EvaluationResult.Success)
    assertEquals("1", (trig as EvaluationResult.Success).formatted)

    val root = CalculatorEngine.evaluate("√(144)+5!", isDegreeMode = true)
    assertTrue(root is EvaluationResult.Success)
    assertEquals("132", (root as EvaluationResult.Success).formatted)
  }

  @Test
  fun divisionByZeroReturnsRussianError() {
    val err = CalculatorEngine.evaluate("42÷0", isDegreeMode = true)
    assertTrue(err is EvaluationResult.Error)
    assertEquals("Деление на ноль", (err as EvaluationResult.Error).messageRu)
  }
}
