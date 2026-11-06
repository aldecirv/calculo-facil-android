package com.calculofacil.app.data

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

object MathEngine {

    sealed class EvalResult {
        data class Success(val value: Double) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    private enum class Type { NUMBER, OPERATOR, FUNCTION, CONSTANT, LPAREN, RPAREN, PERCENT, FACTORIAL }
    private data class Token(val type: Type, val value: String)

    fun evaluate(expression: String, angleUnit: AngleUnit): EvalResult {
        if (expression.isBlank()) return EvalResult.Success(0.0)
        return try {
            val parser = Parser(tokenize(expression), angleUnit)
            val value = parser.parse()
            if (value.isNaN()) EvalResult.Error("Resultado indefinido")
            else if (value.isInfinite()) EvalResult.Error("Valor excede os limites")
            else EvalResult.Success(cleanFloat(value))
        } catch (error: Throwable) {
            EvalResult.Error(error.message ?: "Expressão inválida")
        }
    }

    private fun tokenize(expr: String): List<Token> {
        val normalized = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("³√", "cbrt")
            .replace("√", "sqrt")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("π", "pi")
            .replace("log₁₀", "log")
            .replace(',', '.')
            .trim()
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < normalized.length) {
            val ch = normalized[i]
            if (ch.isWhitespace()) { i++; continue }
            if (ch.isDigit() || (ch == '.' && i + 1 < normalized.length && normalized[i + 1].isDigit())) {
                val sb = StringBuilder()
                sb.append(ch); i++
                while (i < normalized.length && (normalized[i].isDigit() || normalized[i] == '.')) {
                    if (normalized[i] == '.' && sb.contains('.')) break
                    sb.append(normalized[i]); i++
                }
                tokens.add(Token(Type.NUMBER, sb.toString())); continue
            }
            if (ch.isLetter()) {
                val sb = StringBuilder(); sb.append(ch); i++
                while (i < normalized.length && (normalized[i].isLetterOrDigit())) {
                    sb.append(normalized[i]); i++
                }
                when (val id = sb.toString().lowercase()) {
                    "pi" -> tokens.add(Token(Type.CONSTANT, PI.toString()))
                    "e" -> tokens.add(Token(Type.CONSTANT, kotlin.math.E.toString()))
                    "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh",
                    "ln", "log", "log2", "sqrt", "cbrt", "abs", "exp" -> tokens.add(Token(Type.FUNCTION, id))
                    else -> throw IllegalArgumentException("Termo desconhecido: $id")
                }
                continue
            }
            when (ch) {
                '(' -> tokens.add(Token(Type.LPAREN, "("))
                ')' -> tokens.add(Token(Type.RPAREN, ")"))
                '%' -> tokens.add(Token(Type.PERCENT, "%"))
                '!' -> tokens.add(Token(Type.FACTORIAL, "!"))
                '+', '-', '*', '/', '^' -> tokens.add(Token(Type.OPERATOR, ch.toString()))
                else -> throw IllegalArgumentException("Caractere inválido: $ch")
            }
            i++
        }
        val enriched = mutableListOf<Token>()
        tokens.forEach { token ->
            val prev = enriched.lastOrNull()
            if (prev != null) {
                val prevCan = prev.type == Type.NUMBER || prev.type == Type.CONSTANT ||
                    prev.type == Type.RPAREN || prev.type == Type.PERCENT || prev.type == Type.FACTORIAL
                val curCan = token.type == Type.NUMBER || token.type == Type.CONSTANT ||
                    token.type == Type.FUNCTION || token.type == Type.LPAREN
                if (prevCan && curCan) enriched.add(Token(Type.OPERATOR, "*"))
            }
            enriched.add(token)
        }
        return enriched
    }

    private class Parser(private val tokens: List<Token>, private val angleUnit: AngleUnit) {
        private var pos = 0
        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun consume(): Token = tokens.getOrNull(pos++) ?: throw IllegalArgumentException("Expressão incompleta")
        fun parse(): Double {
            if (tokens.isEmpty()) return 0.0
            val result = parseExpression()
            if (pos < tokens.size) throw IllegalArgumentException("Sintaxe inválida próxima a \"${tokens[pos].value}\"")
            return result
        }
        private fun parseExpression(): Double {
            var left = parseTerm()
            while (peek()?.type == Type.OPERATOR && (peek()?.value == "+" || peek()?.value == "-")) {
                val op = consume().value
                val right = parseTerm()
                left = if (op == "+") left + right else left - right
            }
            return left
        }
        private fun parseTerm(): Double {
            var left = parseFactor()
            while (peek()?.type == Type.OPERATOR && (peek()?.value == "*" || peek()?.value == "/")) {
                val op = consume().value
                val right = parseFactor()
                if (op == "*") left *= right else {
                    if (abs(right) < 1e-15) throw IllegalArgumentException("Divisão por zero")
                    left /= right
                }
            }
            return left
        }
        private fun parseFactor(): Double {
            var base = parseUnary()
            if (peek()?.type == Type.OPERATOR && peek()?.value == "^") {
                consume()
                base = Math.pow(base, parseFactor())
            }
            return base
        }
        private fun parseUnary(): Double {
            val token = peek()
            if (token?.type == Type.OPERATOR) {
                if (token.value == "+") { consume(); return parseUnary() }
                if (token.value == "-") { consume(); return -parseUnary() }
            }
            return parsePostfix()
        }
        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (peek()?.type == Type.PERCENT || peek()?.type == Type.FACTORIAL) {
                val next = consume()
                if (next.type == Type.PERCENT) value /= 100.0
                else value = factorial(value)
            }
            return value
        }
        private fun parsePrimary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Expressão incompleta")
            return when (token.type) {
                Type.NUMBER -> { consume(); token.value.toDouble() }
                Type.CONSTANT -> { consume(); token.value.toDouble() }
                Type.FUNCTION -> {
                    val fn = consume().value
                    var hasParen = false
                    if (peek()?.type == Type.LPAREN) { consume(); hasParen = true }
                    val arg = parseExpression()
                    if (hasParen && peek()?.type == Type.RPAREN) consume()
                    applyFunction(fn, arg)
                }
                Type.LPAREN -> {
                    consume()
                    val value = parseExpression()
                    if (peek()?.type == Type.RPAREN) consume()
                    value
                }
                else -> throw IllegalArgumentException("Símbolo inesperado: \"${token.value}\"")
            }
        }
        private fun applyFunction(name: String, x: Double): Double {
            val toRad = { deg: Double -> deg * PI / 180.0 }
            val toDeg = { rad: Double -> rad * 180.0 / PI }
            return when (name) {
                "sin" -> {
                    if (angleUnit == AngleUnit.DEG && x % 180.0 == 0.0) 0.0
                    else sin(if (angleUnit == AngleUnit.DEG) toRad(x) else x)
                }
                "cos" -> {
                    if (angleUnit == AngleUnit.DEG && (x - 90.0) % 180.0 == 0.0) 0.0
                    else cos(if (angleUnit == AngleUnit.DEG) toRad(x) else x)
                }
                "tan" -> {
                    if (angleUnit == AngleUnit.DEG && (x - 90.0) % 180.0 == 0.0) throw IllegalArgumentException("Tangente indefinida")
                    tan(if (angleUnit == AngleUnit.DEG) toRad(x) else x)
                }
                "asin" -> {
                    if (x !in -1.0..1.0) throw IllegalArgumentException("Fora do domínio [-1, 1]")
                    val rad = asin(x); if (angleUnit == AngleUnit.DEG) toDeg(rad) else rad
                }
                "acos" -> {
                    if (x !in -1.0..1.0) throw IllegalArgumentException("Fora do domínio [-1, 1]")
                    val rad = acos(x); if (angleUnit == AngleUnit.DEG) toDeg(rad) else rad
                }
                "atan" -> {
                    val rad = atan(x); if (angleUnit == AngleUnit.DEG) toDeg(rad) else rad
                }
                "sinh" -> sinh(x)
                "cosh" -> cosh(x)
                "tanh" -> tanh(x)
                "ln" -> { if (x <= 0) throw IllegalArgumentException("Logaritmo de número não positivo"); ln(x) }
                "log" -> { if (x <= 0) throw IllegalArgumentException("Logaritmo de número não positivo"); log10(x) }
                "log2" -> { if (x <= 0) throw IllegalArgumentException("Logaritmo de número não positivo"); log2(x) }
                "sqrt" -> { if (x < 0) throw IllegalArgumentException("Raiz de número negativo"); sqrt(x) }
                "cbrt" -> cbrt(x)
                "abs" -> abs(x)
                "exp" -> exp(x)
                else -> throw IllegalArgumentException("Função não suportada")
            }
        }
        private fun factorial(n: Double): Double {
            if (n < 0 || n != Math.floor(n)) throw IllegalArgumentException("Fatorial requer inteiro não-negativo")
            if (n > 170) return Double.POSITIVE_INFINITY
            var result = 1.0
            var i = 2
            while (i <= n.toInt()) { result *= i; i++ }
            return result
        }
    }
}
