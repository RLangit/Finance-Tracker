package com.example

import com.example.ui.ArithmeticEvaluator
import com.example.ui.ExpressionThousandsSeparatorVisualTransformation
import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArithmeticEvaluatorTest {

    @Test
    fun `test subtraction with formatted input`() {
        val input = "200.000 - 50.000"
        assertTrue(ArithmeticEvaluator.containsOperator(input))
        val result = ArithmeticEvaluator.evaluate(input)
        assertNotNull(result)
        assertEquals(150000.0, result!!, 0.001)
        assertEquals("150.000", ArithmeticEvaluator.formatResult(result))
    }

    @Test
    fun `test addition with formatted input`() {
        val input = "100.000 + 75.000"
        assertTrue(ArithmeticEvaluator.containsOperator(input))
        val result = ArithmeticEvaluator.evaluate(input)
        assertNotNull(result)
        assertEquals(175000.0, result!!, 0.001)
        assertEquals("175.000", ArithmeticEvaluator.formatResult(result))
    }

    @Test
    fun `test multiplication with x and asterisk`() {
        val result1 = ArithmeticEvaluator.evaluate("100.000 * 2")
        assertEquals(200000.0, result1!!, 0.001)

        val result2 = ArithmeticEvaluator.evaluate("50.000 x 3")
        assertEquals(150000.0, result2!!, 0.001)
    }

    @Test
    fun `test division with colon and slash`() {
        val result1 = ArithmeticEvaluator.evaluate("1.500.000 : 3")
        assertEquals(500000.0, result1!!, 0.001)

        val result2 = ArithmeticEvaluator.evaluate("600.000 / 2")
        assertEquals(300000.0, result2!!, 0.001)
    }

    @Test
    fun `test operator precedence`() {
        val result = ArithmeticEvaluator.evaluate("50.000 + 25.000 * 2")
        assertEquals(100000.0, result!!, 0.001)
    }

    @Test
    fun `test invalid or incomplete expression`() {
        assertNull(ArithmeticEvaluator.evaluate("200.000 - "))
        assertNull(ArithmeticEvaluator.evaluate("100.000 / 0"))
    }

    @Test
    fun `test expression visual transformation formatting`() {
        val transformation = ExpressionThousandsSeparatorVisualTransformation()
        val result = transformation.filter(AnnotatedString("24000 - 7000"))
        assertEquals("24.000 - 7.000", result.text.text)
    }
}
