package com.calculofacil.app.data

enum class UnitCategory(val title: String) {
    LENGTH("Comprimento"),
    MASS("Massa / Peso"),
    TEMPERATURE("Temperatura"),
    CURRENCY("Moedas"),
    AREA("Área"),
    VOLUME("Volume"),
    SPEED("Velocidade"),
    TIME("Tempo"),
    DIGITAL("Dados digitais")
}

data class MeasureUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val factor: Double = 1.0,
    val kind: UnitCategory = UnitCategory.LENGTH
)

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val rateToUsd: Double
)

object UnitsData {
    val units: Map<UnitCategory, List<MeasureUnit>> = mapOf(
        UnitCategory.LENGTH to listOf(
            MeasureUnit("m", "Metro", "m", 1.0), MeasureUnit("km", "Quilômetro", "km", 1000.0),
            MeasureUnit("cm", "Centímetro", "cm", 0.01), MeasureUnit("mm", "Milímetro", "mm", 0.001),
            MeasureUnit("in", "Polegada", "pol", 0.0254), MeasureUnit("ft", "Pé", "ft", 0.3048),
            MeasureUnit("yd", "Jarda", "yd", 0.9144), MeasureUnit("mi", "Milha", "mi", 1609.344),
            MeasureUnit("nmi", "Milha náutica", "NM", 1852.0)
        ),
        UnitCategory.MASS to listOf(
            MeasureUnit("kg", "Quilograma", "kg", 1.0), MeasureUnit("g", "Grama", "g", 0.001),
            MeasureUnit("mg", "Miligrama", "mg", 0.000001), MeasureUnit("t", "Tonelada", "t", 1000.0),
            MeasureUnit("lb", "Libra", "lb", 0.45359237), MeasureUnit("oz", "Onça", "oz", 0.02834952),
            MeasureUnit("ct", "Quilate", "ct", 0.0002)
        ),
        UnitCategory.TEMPERATURE to listOf(
            MeasureUnit("c", "Celsius", "°C"), MeasureUnit("f", "Fahrenheit", "°F"), MeasureUnit("k", "Kelvin", "K")
        ),
        UnitCategory.AREA to listOf(
            MeasureUnit("m2", "Metro quadrado", "m²", 1.0), MeasureUnit("km2", "Quilômetro quadrado", "km²", 1_000_000.0),
            MeasureUnit("cm2", "Centímetro quadrado", "cm²", 0.0001), MeasureUnit("ha", "Hectare", "ha", 10_000.0),
            MeasureUnit("ac", "Acre", "ac", 4046.856422), MeasureUnit("ft2", "Pé quadrado", "ft²", 0.092903)
        ),
        UnitCategory.VOLUME to listOf(
            MeasureUnit("l", "Litro", "L", 1.0), MeasureUnit("ml", "Mililitro", "mL", 0.001),
            MeasureUnit("m3", "Metro cúbico", "m³", 1000.0), MeasureUnit("gal", "Galão americano", "gal", 3.78541),
            MeasureUnit("floz", "Onça fluida", "fl oz", 0.0295735), MeasureUnit("cup", "Xícara", "xícara", 0.24),
            MeasureUnit("tbsp", "Colher de sopa", "c. sopa", 0.015)
        ),
        UnitCategory.SPEED to listOf(
            MeasureUnit("kmh", "Quilômetro por hora", "km/h", 1.0 / 3.6), MeasureUnit("ms", "Metro por segundo", "m/s", 1.0),
            MeasureUnit("mph", "Milha por hora", "mph", 0.44704), MeasureUnit("knot", "Nó", "kn", 0.514444),
            MeasureUnit("mach", "Mach", "Ma", 343.0)
        ),
        UnitCategory.TIME to listOf(
            MeasureUnit("s", "Segundo", "s", 1.0), MeasureUnit("min", "Minuto", "min", 60.0),
            MeasureUnit("h", "Hora", "h", 3600.0), MeasureUnit("d", "Dia", "d", 86400.0),
            MeasureUnit("wk", "Semana", "sem", 604800.0), MeasureUnit("mo", "Mês (30 dias)", "mês", 2592000.0),
            MeasureUnit("yr", "Ano (365 dias)", "ano", 31536000.0)
        ),
        UnitCategory.DIGITAL to listOf(
            MeasureUnit("b", "Byte", "B", 1.0), MeasureUnit("kb", "Quilobyte", "KB", 1024.0),
            MeasureUnit("mb", "Megabyte", "MB", 1024.0 * 1024), MeasureUnit("gb", "Gigabyte", "GB", 1024.0 * 1024 * 1024),
            MeasureUnit("tb", "Terabyte", "TB", 1024.0 * 1024 * 1024 * 1024)
        )
    )

    val currencies = listOf(
        Currency("BRL", "Real brasileiro", "R$", 5.65),
        Currency("USD", "Dólar americano", "$", 1.0),
        Currency("EUR", "Euro", "€", 0.92),
        Currency("GBP", "Libra esterlina", "£", 0.78),
        Currency("ARS", "Peso argentino", "$", 1015.0),
        Currency("JPY", "Iene japonês", "¥", 153.5),
        Currency("CAD", "Dólar canadense", "C$", 1.39),
        Currency("CHF", "Franco suíço", "CHF", 0.87),
        Currency("CNY", "Yuan chinês", "¥", 7.15),
        Currency("AUD", "Dólar australiano", "A$", 1.52),
        Currency("CLP", "Peso chileno", "$", 955.0),
        Currency("UYU", "Peso uruguaio", "$", 41.5)
    )

    fun convert(value: Double, from: MeasureUnit, to: MeasureUnit, category: UnitCategory): Double {
        if (category == UnitCategory.TEMPERATURE) {
            val celsius = when (from.id) {
                "f" -> (value - 32.0) * 5.0 / 9.0
                "k" -> value - 273.15
                else -> value
            }
            return when (to.id) {
                "f" -> celsius * 9.0 / 5.0 + 32.0
                "k" -> celsius + 273.15
                else -> celsius
            }
        }
        return value * from.factor / to.factor
    }

    fun convertCurrency(value: Double, from: Currency, to: Currency): Double =
        value / from.rateToUsd * to.rateToUsd
}
