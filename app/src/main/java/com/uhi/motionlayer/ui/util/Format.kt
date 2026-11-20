package com.uhi.motionlayer.ui.util

import java.util.Locale

/**
 * 파일 크기를 짧게 쓴다. 1MB를 1,000,000바이트로 센다: 10MB 이상은 정수("38MB"), 0.1MB 이상은 소수 첫째 자리("1.2MB", "0.4MB"),
 * 그보다 작으면 KB("80KB").
 */
fun formatFileSize(bytes: Long): String = when {
    bytes >= 10_000_000 -> "${Math.round(bytes / 1_000_000.0)}MB"
    bytes >= 100_000 -> "${String.format(Locale.US, "%.1f", bytes / 1_000_000.0)}MB"
    else -> "${Math.round(bytes / 1_000.0)}KB"
}

/**
 * 소수 [digits]째 자리까지 쓴다("0.6", digits=2면 "0.60"). 언어와 상관없이 점(.)을 쓴다.
 * 아주 작은 음수가 0으로 반올림되면 "-0.00"이 되므로 부호를 뗀다.
 */
fun formatDecimal(value: Float, digits: Int = 1): String {
    val text = String.format(Locale.US, "%.${digits}f", value)
    return if (text.startsWith("-") && text.all { it == '-' || it == '0' || it == '.' }) text.drop(1) else text
}
