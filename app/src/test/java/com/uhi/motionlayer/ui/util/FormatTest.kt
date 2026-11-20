package com.uhi.motionlayer.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun formatDecimal_digits() {
        assertEquals("0.6", formatDecimal(0.6f))
        assertEquals("9.71", formatDecimal(9.71f, digits = 2))
        assertEquals("0.00", formatDecimal(0f, digits = 2))
    }

    @Test
    fun formatDecimal_keepsRealNegatives() {
        assertEquals("-0.5", formatDecimal(-0.5f))
        assertEquals("-0.03", formatDecimal(-0.03f, digits = 2))
    }

    @Test
    fun formatFileSize_matchesFigmaExamples() {
        assertEquals("1.2MB", formatFileSize(1_200_000))
        assertEquals("38MB", formatFileSize(38_000_000))
        assertEquals("0.4MB", formatFileSize(400_000))
        assertEquals("80KB", formatFileSize(80_000))
    }

    @Test
    fun formatDecimal_dropsNegativeZero() {
        assertEquals("0.00", formatDecimal(-0.001f, digits = 2))
        assertEquals("0.0", formatDecimal(-0.04f))
    }
}
