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
