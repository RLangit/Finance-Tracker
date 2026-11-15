package com.example.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class ThousandsSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val formattedText = StringBuilder()
        val length = originalText.length
        for (i in 0 until length) {
            if (i > 0 && (length - i) % 3 == 0) {
                formattedText.append('.')
            }
            formattedText.append(originalText[i])
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var dots = 0
                for (i in 0 until offset) {
                    if (i > 0 && (length - i) % 3 == 0) {
                        dots++
                    }
                }
                return (offset + dots).coerceIn(0, formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var dots = 0
                for (i in 0 until offset) {
                    if (i < formattedText.length && formattedText[i] == '.') {
                        dots++
                    }
                }
                return (offset - dots).coerceIn(0, length)
            }
        }
        return TransformedText(AnnotatedString(formattedText.toString()), offsetMapping)
    }
}

class ExpressionThousandsSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formatted = StringBuilder()
        val originalToTransformedMap = IntArray(original.length + 1)

        var i = 0
        val n = original.length
        while (i < n) {
            if (original[i].isDigit()) {
                val start = i
                while (i < n && original[i].isDigit()) {
                    i++
                }
                val digits = original.substring(start, i)
                val numLen = digits.length
                for (j in 0 until numLen) {
                    if (j > 0 && (numLen - j) % 3 == 0) {
                        formatted.append('.')
                    }
                    originalToTransformedMap[start + j] = formatted.length
                    formatted.append(digits[j])
                }
            } else {
                originalToTransformedMap[i] = formatted.length
                formatted.append(original[i])
                i++
            }
        }
        originalToTransformedMap[n] = formatted.length

        val transformedStr = formatted.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, original.length)
                return originalToTransformedMap[clamped]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformedStr.length)
                var res = 0
                for (idx in 0..original.length) {
                    if (originalToTransformedMap[idx] <= clamped) {
                        res = idx
                    } else {
                        break
                    }
                }
                return res
            }
        }

        return TransformedText(AnnotatedString(transformedStr), offsetMapping)
    }
}
