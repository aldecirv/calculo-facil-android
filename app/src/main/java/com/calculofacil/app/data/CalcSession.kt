package com.calculofacil.app.data

object CalcSession {
    var expression: String = ""
        private set
    var preview: String = ""
        private set
    var error: String = ""
        private set
    private var listeners = mutableListOf<() -> Unit>()

    fun subscribe(listener: () -> Unit): () -> Unit {
        listeners.add(listener)
        listener()
        return { listeners.remove(listener); Unit }
    }

    private fun notifyListeners() = listeners.toList().forEach { it() }

    fun press(key: String, category: String) {
        when (key) {
            "AC" -> clear()
            "DEL" -> backspace()
            "=" -> evaluateEquals(category)
            "NEG" -> {
                expression = if (expression.startsWith("-(") && expression.endsWith(")")) {
                    expression.substring(2, expression.length - 1)
                } else if (expression.isBlank()) {
                    "-"
                } else {
                    "-($expression)"
                }
                refresh()
            }
            "()" -> appendSmartParenthesis()
            "±" -> {
                expression = if (expression.startsWith("-")) expression.drop(1) else "-$expression"
                refresh()
            }
            else -> {
                val value = when (key) {
                    "×" -> "*"
                    "÷" -> "/"
                    "−" -> "-"
                    else -> key
                }
                expression += value
                refresh()
            }
        }
    }

    private fun appendSmartParenthesis() {
        val open = expression.count { it == '(' }
        val close = expression.count { it == ')' }
        val last = expression.lastOrNull()
        val shouldClose = open > close && last != null && (last.isDigit() || last in "πe%)!")
        expression += if (shouldClose) ")" else "("
        refresh()
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            refresh()
        }
    }

    fun clear() {
        expression = ""
        preview = ""
        error = ""
        notifyListeners()
    }

    fun setExpression(value: String) {
        expression = value
        refresh()
    }

    fun refresh() {
        if (expression.isBlank()) {
            preview = ""
            error = ""
            notifyListeners()
            return
        }
        when (val result = MathEngine.evaluate(expression, CalcStore.settings.angleUnit)) {
            is MathEngine.EvalResult.Success -> {
                preview = formatNumber(result.value, CalcStore.settings)
                error = ""
            }
            is MathEngine.EvalResult.Error -> {
                preview = ""
                error = result.message
            }
        }
        notifyListeners()
    }

    private fun evaluateEquals(category: String) {
        if (expression.isBlank()) return
        when (val result = MathEngine.evaluate(expression, CalcStore.settings.angleUnit)) {
            is MathEngine.EvalResult.Success -> {
                val resultText = formatNumber(result.value, CalcStore.settings)
                val original = expression
                expression = formatPlain(result.value, CalcStore.settings)
                preview = resultText
                error = ""
                CalcStore.addHistory(
                    CalcHistory(
                        expression = original,
                        result = resultText,
                        category = category,
                        label = if (category == "Científico") "Cálculo científico" else "Cálculo básico"
                    )
                )
                notifyListeners()
            }
            is MathEngine.EvalResult.Error -> {
                error = result.message
                preview = ""
                notifyListeners()
            }
        }
    }

    fun memoryAction(action: String) {
        when (action) {
            "MC" -> CalcStore.setMemory(0.0)
            "MR" -> {
                if (CalcStore.memory() != 0.0) {
                    expression += formatPlain(CalcStore.memory(), CalcStore.settings)
                    refresh()
                }
            }
            "M+" -> CalcStore.setMemory(CalcStore.memory() + currentValue())
            "M-" -> CalcStore.setMemory(CalcStore.memory() - currentValue())
        }
    }

    private fun currentValue(): Double {
        val source = if (preview.isNotBlank()) expression else expression
        return when (val result = MathEngine.evaluate(source, CalcStore.settings.angleUnit)) {
            is MathEngine.EvalResult.Success -> result.value
            else -> 0.0
        }
    }
}
