package com.calculofacil.app.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.calculofacil.app.R
import com.calculofacil.app.data.CalcHistory
import com.calculofacil.app.data.CalcStore
import com.calculofacil.app.data.formatNumber
import com.calculofacil.app.data.parseInputNumber
import com.calculofacil.app.databinding.FragmentFinanceBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.abs
import kotlin.math.pow

class FinanceFragment : Fragment() {

    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!

    private enum class Tool(val title: String, val description: String, val icon: Int) {
        DISCOUNT("Desconto", "Preço final e economia", R.drawable.ic_tag),
        PERCENTAGE("Porcentagem", "Parte, proporção e variação", R.drawable.ic_percent),
        RULE("Regra de 3", "Proporção direta ou inversa", R.drawable.ic_divide),
        TIP("Gorjeta & conta", "Divida a conta entre pessoas", R.drawable.ic_money),
        COMPOUND("Juros compostos", "Investimento com aportes", R.drawable.ic_savings),
        LOAN("Financiamento", "PRICE ou SAC", R.drawable.ic_receipt)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Tool.entries.forEach { tool -> binding.toolsContainer.addView(makeCard(tool)) }
    }

    private fun makeCard(tool: Tool): View {
        val card = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(8) }
            radius = dp(17).toFloat()
            setCardBackgroundColor(requireContext().getColor(R.color.card))
            strokeWidth = dp(1)
            strokeColor = requireContext().getColor(R.color.border)
            cardElevation = 0f
            isClickable = true
            isFocusable = true
            setOnClickListener { openTool(tool) }
        }
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(13), dp(14), dp(13))
        }
        val icon = android.widget.ImageView(requireContext()).apply {
            setImageResource(tool.icon)
            setColorFilter(requireContext().getColor(R.color.accent))
            layoutParams = LinearLayout.LayoutParams(dp(23), dp(23))
        }
        row.addView(icon)
        val texts = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(11) }
        }
        texts.addView(TextView(requireContext()).apply {
            text = tool.title
            setTextColor(requireContext().getColor(R.color.text_primary))
            textSize = 13f
            typeface = resources.getFont(R.font.poppins_semibold)
        })
        texts.addView(TextView(requireContext()).apply {
            text = tool.description
            setTextColor(requireContext().getColor(R.color.text_tertiary))
            textSize = 10.5f
        })
        row.addView(texts)
        row.addView(android.widget.ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_arrow_up)
            rotation = 90f
            setColorFilter(requireContext().getColor(R.color.text_tertiary))
            layoutParams = LinearLayout.LayoutParams(dp(17), dp(17))
        })
        card.addView(row)
        return card
    }

    private fun openTool(tool: Tool) {
        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(4))
        }
        val fields = mutableListOf<EditText>()
        var compute: () -> Pair<String, String>? = { null }

        fun addField(label: String, suffix: String = ""): EditText {
            val til = TextInputLayout(requireContext(), null, com.google.android.material.R.attr.textInputOutlinedStyle).apply {
                hint = if (suffix.isBlank()) label else "$label ($suffix)"
                boxBackgroundColor = requireContext().getColor(R.color.surface)
                boxStrokeColor = requireContext().getColor(R.color.accent)
                setBoxCornerRadii(dp(12).toFloat(), dp(12).toFloat(), dp(12).toFloat(), dp(12).toFloat())
            }
            val edit = TextInputEditText(requireContext()).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                setTextColor(requireContext().getColor(R.color.text_primary))
                textSize = 14f
                setSingleLine(true)
            }
            til.addView(edit)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) }
            content.addView(til, lp)
            fields.add(edit)
            return edit
        }

        fun addSwitch(text: String): MaterialSwitch {
            val sw = MaterialSwitch(requireContext()).apply {
                this.text = text
                setTextColor(requireContext().getColor(R.color.text_secondary))
                textSize = 11.5f
                setPadding(0, dp(5), 0, dp(2))
            }
            content.addView(sw)
            return sw
        }

        val result = TextView(requireContext()).apply {
            text = "Preencha os campos para calcular."
            setTextColor(requireContext().getColor(R.color.text_secondary))
            textSize = 12f
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundResource(R.drawable.bg_display)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) }
        }

        fun updateResult() {
            val pair = runCatching { compute() }.getOrNull()
            result.text = pair?.second ?: "Preencha os campos para calcular."
        }

        fun attachUpdates() {
            fields.forEach { edit ->
                edit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                    override fun afterTextChanged(s: Editable?) = updateResult()
                })
            }
        }

        when (tool) {
            Tool.DISCOUNT -> {
                val price = addField("Preço original")
                val discount = addField("Desconto", "%")
                compute = {
                    val p = number(price); val d = number(discount)
                    if (p == null || d == null) null else {
                        val dd = d.coerceIn(0.0, 100.0)
                        val saving = p * dd / 100.0
                        val final = p - saving
                        "Desconto de ${fmt(dd)}%" to "Preço final: ${money(final)}\nEconomia: ${money(saving)}\nFórmula: preço − (preço × desconto ÷ 100)"
                    }
                }
            }
            Tool.PERCENTAGE -> {
                val mode = MaterialAutoCompleteTextView(requireContext()).apply {
                    setTextColor(requireContext().getColor(R.color.text_primary))
                    textSize = 13f
                    setAdapter(ArrayAdapter(requireContext(), R.layout.item_dropdown, listOf("A% de B", "A é quanto % de B", "Variação de A para B")))
                    setText("A% de B", false)
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    setBackgroundResource(R.drawable.bg_pill)
                }
                content.addView(mode, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
                val a = addField("Valor A")
                val b = addField("Valor B")
                mode.setOnItemClickListener { _, _, position, _ ->
                    a.hint = when (position) { 0 -> "Porcentagem A (%)"; 1 -> "Valor A"; else -> "Valor inicial A" }
                    b.hint = when (position) { 0 -> "Valor B"; 1 -> "Valor base B"; else -> "Valor final B" }
                    updateResult()
                }
                compute = {
                    val va = number(a); val vb = number(b)
                    if (va == null || vb == null) null else when (mode.text.toString()) {
                        "A é quanto % de B" -> if (vb == 0.0) "Porcentagem" to "Indefinido: B não pode ser zero." else "Porcentagem" to "${fmt(va / vb * 100)}% de B\nFórmula: (A ÷ B) × 100"
                        "Variação de A para B" -> if (va == 0.0) "Variação" to "Indefinido: A não pode ser zero." else {
                            val change = (vb - va) / va * 100
                            "Variação" to "${if (change >= 0) "+" else ""}${fmt(change)}%\n${if (change >= 0) "Aumento" else "Redução"} de ${fmt(abs(change))}%."
                        }
                        else -> "Porcentagem" to "${fmt(va / 100.0 * vb)}\nFórmula: (A ÷ 100) × B"
                    }
                }
                attachUpdates()
            }
            Tool.RULE -> {
                val a = addField("Valor A"); val b = addField("Valor B"); val c = addField("Valor C")
                val inverse = addSwitch("Proporção inversa")
                inverse.setOnCheckedChangeListener { _, _ -> updateResult() }
                compute = {
                    val va = number(a); val vb = number(b); val vc = number(c)
                    if (va == null || vb == null || vc == null) null else if (inverse.isChecked) {
                        if (vc == 0.0) "Regra de 3 inversa" to "C não pode ser zero." else "Regra de 3 inversa" to "X = ${fmt(va * vb / vc)}\nFórmula: X = (A × B) ÷ C"
                    } else {
                        if (va == 0.0) "Regra de 3" to "A não pode ser zero." else "Regra de 3" to "X = ${fmt(vb * vc / va)}\nFórmula: X = (B × C) ÷ A"
                    }
                }
                attachUpdates()
            }
            Tool.TIP -> {
                val bill = addField("Conta")
                val tip = addField("Gorjeta", "%")
                val people = addField("Pessoas")
                compute = {
                    val b = number(bill); val t = number(tip); val p = number(people)
                    if (b == null || t == null || p == null || p < 1) null else {
                        val tipValue = b * t / 100.0
                        val total = b + tipValue
                        "Gorjeta" to "Gorjeta: ${money(tipValue)}\nTotal: ${money(total)}\nPor pessoa: ${money(total / p)}"
                    }
                }
                attachUpdates()
            }
            Tool.COMPOUND -> {
                val principal = addField("Capital inicial")
                val monthly = addField("Aporte mensal")
                val rate = addField("Taxa", "%")
                val months = addField("Período", "meses")
                val annual = addSwitch("Taxa anual")
                annual.setOnCheckedChangeListener { _, _ -> updateResult() }
                compute = {
                    val p = number(principal); val m = number(monthly) ?: 0.0; val r = number(rate); val n = number(months)
                    if (p == null || r == null || n == null || n < 1) null else {
                        val monthlyRate = if (annual.isChecked) (1 + r / 100.0).pow(1.0 / 12.0) - 1 else r / 100.0
                        val factor = (1 + monthlyRate).pow(n)
                        val future = p * factor + if (monthlyRate == 0.0) m * n else m * (factor - 1) / monthlyRate
                        val invested = p + m * n
                        "Juros compostos" to "Montante: ${money(future)}\nInvestido: ${money(invested)}\nJuros: ${money(future - invested)}"
                    }
                }
                attachUpdates()
            }
            Tool.LOAN -> {
                val principal = addField("Valor financiado")
                val rate = addField("Taxa", "%")
                val months = addField("Prazo", "meses")
                val annual = addSwitch("Taxa anual")
                val system = MaterialAutoCompleteTextView(requireContext()).apply {
                    setTextColor(requireContext().getColor(R.color.text_primary))
                    textSize = 13f
                    setAdapter(ArrayAdapter(requireContext(), R.layout.item_dropdown, listOf("PRICE (parcela fixa)", "SAC (amortização constante)")))
                    setText("PRICE (parcela fixa)", false)
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    setBackgroundResource(R.drawable.bg_pill)
                }
                content.addView(system, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
                system.setOnItemClickListener { _, _, _, _ -> updateResult() }
                annual.setOnCheckedChangeListener { _, _ -> updateResult() }
                compute = {
                    val p = number(principal); val r = number(rate); val n = number(months)
                    if (p == null || r == null || n == null || n < 1) null else {
                        val i = if (annual.isChecked) (1 + r / 100.0).pow(1.0 / 12.0) - 1 else r / 100.0
                        if (system.text.toString().startsWith("SAC")) {
                            val amort = p / n
                            val first = amort + p * i
                            val last = amort + amort * i
                            val interest = p * i * (n + 1) / 2
                            "Financiamento SAC" to "1ª parcela: ${money(first)}\nÚltima: ${money(last)}\nTotal: ${money(p + interest)}\nJuros: ${money(interest)}"
                        } else {
                            val pmt = if (i == 0.0) p / n else p * i * (1 + i).pow(n) / ((1 + i).pow(n) - 1)
                            "Financiamento PRICE" to "Parcela: ${money(pmt)}\nTotal: ${money(pmt * n)}\nJuros: ${money(pmt * n - p)}"
                        }
                    }
                }
                attachUpdates()
            }
        }

        content.addView(result)
        attachUpdates()
        updateResult()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(tool.title)
            .setView(content)
            .setNegativeButton("Fechar", null)
            .setPositiveButton("Salvar no histórico", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pair = runCatching { compute() }.getOrNull()
                if (pair == null) {
                    Toast.makeText(requireContext(), "Preencha os campos", Toast.LENGTH_SHORT).show()
                } else {
                    CalcStore.addHistory(CalcHistory(expression = "${tool.title}: ${pair.first}", result = pair.second, category = "Financeiro", label = tool.title))
                    Toast.makeText(requireContext(), "Resultado salvo no histórico", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun number(edit: EditText): Double? = parseInputNumber(edit.text?.toString().orEmpty())

    private fun fmt(value: Double): String = formatNumber(value, CalcStore.settings)
    private fun money(value: Double): String = "R$ ${formatNumber(value, CalcStore.settings)}"
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
