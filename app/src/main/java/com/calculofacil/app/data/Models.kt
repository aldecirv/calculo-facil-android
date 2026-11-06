package com.calculofacil.app.data

import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

enum class AngleUnit(val label: String) { DEG("DEG"), RAD("RAD") }
enum class NumberFormatKind(val label: String) { PT_BR("pt-BR"), EN_US("en-US") }

data class CalcSettings(
    var angleUnit: AngleUnit = AngleUnit.DEG,
    var numberFormat: NumberFormatKind = NumberFormatKind.PT_BR,
    var precision: Int = 0, // 0 = automático
    var vibration: Boolean = true
)

data class CalcHistory(
    val id: String = UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Básico",
    val label: String = ""
) {
    val dateLabel: String
        get() = java.text.SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
            .format(java.util.Date(timestamp))
}

fun parseInputNumber(text: String): Double? {
    val raw = text.trim().replace(" ", "")
    if (raw.isBlank()) return null
    val normalized = when {
        raw.contains(',') -> raw.replace(".", "").replace(',', '.')
        raw.count { it == '.' } == 1 -> {
            val decimals = raw.substringAfterLast('.')
            // Em pt-BR, "1.234" normalmente é milhar; com 1 ou 2 casas, tratamos como decimal.
            if (decimals.length in 1..2) raw else raw.replace(".", "")
        }
        else -> raw.replace(".", "")
    }
    return normalized.toDoubleOrNull()
}

fun formatNumber(value: Double, settings: CalcSettings, grouped: Boolean = true): String {
    if (value.isNaN()) return "Indefinido"
    if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
    val abs = kotlin.math.abs(value)
    if ((abs >= 1e14 || (abs < 1e-6 && abs > 0.0)) && value != 0.0) {
        val exp = String.format(Locale.US, "%.6e", value)
        return if (settings.numberFormat == NumberFormatKind.PT_BR) exp.replace('.', ',') else exp
    }
    val locale = if (settings.numberFormat == NumberFormatKind.PT_BR) Locale("pt", "BR") else Locale.US
    val nf = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = if (settings.precision == 0) 10 else settings.precision
        minimumFractionDigits = if (settings.precision == 0) 0 else settings.precision
        isGroupingUsed = grouped
    }
    return nf.format(if (settings.precision == 0) cleanFloat(value) else value)
}

fun formatPlain(value: Double, settings: CalcSettings): String {
    val abs = kotlin.math.abs(value)
    if (abs >= 1e14 || (abs < 1e-6 && abs > 0.0)) return formatNumber(value, settings, false)
    val decimals = if (settings.precision == 0) 10 else settings.precision
    val locale = if (settings.numberFormat == NumberFormatKind.PT_BR) Locale("pt", "BR") else Locale.US
    val nf = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = decimals
        minimumFractionDigits = 0
        isGroupingUsed = false
    }
    return nf.format(if (settings.precision == 0) cleanFloat(value) else value)
}

fun cleanFloat(value: Double): Double {
    if (kotlin.math.abs(value) < 1e-15) return 0.0
    return java.math.BigDecimal.valueOf(value).round(java.math.MathContext(14)).toDouble()
}
