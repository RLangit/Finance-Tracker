package com.example.ui

import java.util.Stack
import java.util.Locale

object ArithmeticEvaluator {

    fun containsOperator(input: String): Boolean {
        if (input.isBlank()) return false
        val stripped = input.trim()
        val operators = listOf('+', '-', '*', 'x', 'X', '×', ':', '/', '÷')
        for (i in 1 until stripped.length) {
            if (stripped[i] in operators) return true
        }
        return false
    }

    fun evaluate(input: String): Double? {
        if (input.isBlank()) return null
        return try {
            val normalized = normalizeInput(input)
            val tokens = tokenize(normalized)
            if (tokens.isEmpty()) null else parseAndEvaluate(tokens)
        } catch (_: Exception) {
            null
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return ""
        val rounded = Math.round(value * 100.0) / 100.0
        return if (rounded % 1.0 == 0.0) {
            String.format(Locale.GERMANY, "%,.0f", rounded)
        } else {
            String.format(Locale.GERMANY, "%,.2f", rounded)
        }
    }

    private fun normalizeInput(input: String): String {
        return input
            .replace(":", "/")
            .replace("÷", "/")
            .replace("x", "*")
            .replace("X", "*")
            .replace("×", "*")
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var i = 0
        val len = input.length

        while (i < len) {
            val c = input[i]
            when {
                c.isWhitespace() -> {
                    if (sb.isNotEmpty()) {
                        tokens.add(sb.toString())
                        sb.clear()
                    }
                    i++
                }
                c in listOf('+', '-', '*', '/', '(', ')') -> {
                    if (sb.isNotEmpty()) {
                        tokens.add(sb.toString())
                        sb.clear()
                    }
                    if ((c == '-' || c == '+') && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/", "("))) {
                        sb.append(c)
                    } else {
                        tokens.add(c.toString())
                    }
                    i++
                }
                else -> {
                    sb.append(c)
                    i++
                }
            }
        }
        if (sb.isNotEmpty()) {
            tokens.add(sb.toString())
        }

        return tokens
    }

    private fun parseAndEvaluate(tokens: List<String>): Double? {
        val values = Stack<Double>()
        val ops = Stack<Char>()

        var index = 0
        while (index < tokens.size) {
            val token = tokens[index]

            if (isNumberToken(token)) {
                val num = parseNumberToken(token) ?: return null
                values.push(num)
            } else if (token == "(") {
                ops.push('(')
            } else if (token == ")") {
                while (ops.isNotEmpty() && ops.peek() != '(') {
                    val result = applyOp(ops.pop(), values.pop(), values.pop()) ?: return null
                    values.push(result)
                }
                if (ops.isNotEmpty() && ops.peek() == '(') {
                    ops.pop()
                } else {
                    return null
                }
            } else if (token.length == 1 && token[0] in listOf('+', '-', '*', '/')) {
                val op = token[0]
                while (ops.isNotEmpty() && hasPrecedence(op, ops.peek())) {
                    if (values.size < 2) return null
                    val val2 = values.pop()
                    val val1 = values.pop()
                    val result = applyOp(ops.pop(), val2, val1) ?: return null
                    values.push(result)
                }
                ops.push(op)
            } else {
                return null
            }
            index++
        }

        while (ops.isNotEmpty()) {
            if (values.size < 2) return null
            val op = ops.pop()
            if (op == '(' || op == ')') return null
            val val2 = values.pop()
            val val1 = values.pop()
            val result = applyOp(op, val2, val1) ?: return null
            values.push(result)
        }

        return if (values.size == 1) values.pop() else null
    }

    private fun isNumberToken(token: String): Boolean {
        if (token.isEmpty()) return false
        val clean = token.replace("-", "").replace("+", "")
        return clean.any { it.isDigit() }
    }

    private fun parseNumberToken(token: String): Double? {
        var clean = token.trim()
        val isNegative = clean.startsWith("-")
        if (isNegative) clean = clean.substring(1)
        val isPositive = clean.startsWith("+")
        if (isPositive) clean = clean.substring(1)

        var stripped = clean
        if (stripped.contains(".")) {
            val parts = stripped.split(".")
            val isThousands = parts.size > 1 && parts.subList(1, parts.size).all { it.length == 3 && it.all { ch -> ch.isDigit() } }
            if (isThousands) {
                stripped = stripped.replace(".", "")
            }
        }
        stripped = stripped.replace(",", ".")
        val num = stripped.toDoubleOrNull() ?: return null
        return if (isNegative) -num else num
    }

    private fun hasPrecedence(op1: Char, op2: Char): Boolean {
        if (op2 == '(' || op2 == ')') return false
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false
        return true
    }

    private fun applyOp(op: Char, b: Double, a: Double): Double? {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> {
                if (b == 0.0) null else a / b
            }
            else -> null
        }
    }
}
