package com.calculofacil.app.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.calculofacil.app.R
import com.calculofacil.app.data.AngleUnit
import com.calculofacil.app.data.CalcSession
import com.calculofacil.app.data.CalcStore
import com.calculofacil.app.databinding.FragmentCalculatorBinding
import com.google.android.material.button.MaterialButton

class CalculatorFragment : Fragment() {

    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!
    private var unsubscribeSession: (() -> Unit)? = null
    private var unsubscribeStore: (() -> Unit)? = null
    private var scientific = false
    private var second = false
    private var hyp = false
    private var angleKey: MaterialButton? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        scientific = arguments?.getBoolean(ARG_SCIENTIFIC) == true
        binding.modeLabel.text = if (scientific) "CÁLCULO CIENTÍFICO" else "CÁLCULO BÁSICO"
        binding.scientificPanel.visibility = if (scientific) View.VISIBLE else View.GONE
        if (scientific) binding.displayCard.minimumHeight = dp(136)
        buildKeypad()
        if (scientific) buildScientificPanel()
        binding.mc.setOnClickListener { action("MC") }
        binding.mr.setOnClickListener { action("MR") }
        binding.mPlus.setOnClickListener { action("M+") }
        binding.mMinus.setOnClickListener { action("M-") }
        binding.backspace.setOnClickListener { action("DEL") }
        binding.copy.setOnClickListener { copy() }
        unsubscribeSession = CalcSession.subscribe { render() }
        unsubscribeStore = CalcStore.subscribe { updateMeta() }
        updateMeta()
    }

    private fun buildKeypad() {
        val rows = listOf(
            listOf("AC", "()", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("±", "0", ",", "=")
        )
        binding.keypad.removeAllViews()
        rows.forEach { row ->
            val linear = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(0, 4, 0, 0)
            }
            row.forEachIndexed { index, key ->
                val button = keyButton(key, if (key == "=") KeyStyle.EQUALS else if (key == "AC") KeyStyle.DANGER else if (key in listOf("÷", "×", "−", "+")) KeyStyle.OPERATOR else if (key == "()" || key == "%" || key == "±") KeyStyle.FUNCTION else KeyStyle.NUMBER)
                val lp = LinearLayout.LayoutParams(0, dp(if (scientific) 45 else 52), 1f)
                lp.marginStart = if (index == 0) 0 else dp(4)
                linear.addView(button, lp)
                button.setOnClickListener { press(key) }
            }
            binding.keypad.addView(linear)
        }
    }

    private fun buildScientificPanel() {
        val panel = binding.scientificPanel
        panel.removeAllViews()
        val first = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        val degree = keyButton(if (CalcStore.settings.angleUnit == AngleUnit.DEG) "DEG" else "RAD", KeyStyle.SCIENTIFIC_MODE)
        degree.setOnClickListener {
            CalcStore.updateSettings { it.angleUnit = if (it.angleUnit == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG }
            buildScientificPanel()
            updateMeta()
        }
        angleKey = degree
        first.addView(degree, keyLp(0))
        listOf("2nd", "hyp", "π", "e").forEachIndexed { index, label ->
            val b = keyButton(label, KeyStyle.SCIENTIFIC)
            b.setOnClickListener {
                when (label) {
                    "2nd" -> { second = !second; buildScientificPanel() }
                    "hyp" -> { hyp = !hyp; buildScientificPanel() }
                    else -> press(label)
                }
            }
            first.addView(b, keyLp(index + 1))
        }
        panel.addView(first)

        val secondRow = listOf("sin", "cos", "tan", "ln", "log")
        val row2 = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        secondRow.forEachIndexed { index, label ->
            val actual = when {
                hyp -> "${label}h("
                second && label in listOf("sin", "cos", "tan") -> "a${label}("
                second && label == "ln" -> "exp("
                second && label == "log" -> "10^("
                else -> "$label("
            }
            val display = when {
                hyp -> "${label}h"
                second && label in listOf("sin", "cos", "tan") -> "${label}⁻¹"
                second && label == "ln" -> "eˣ"
                second && label == "log" -> "10ˣ"
                else -> label
            }
            val b = keyButton(display, KeyStyle.SCIENTIFIC)
            b.setOnClickListener { press(actual) }
            row2.addView(b, keyLp(index))
        }
        panel.addView(row2)

        val row3 = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("√" to "sqrt(", "x²" to "^2", "xʸ" to "^", "!" to "!", "abs" to "abs(").forEachIndexed { index, pair ->
            val b = keyButton(pair.first, KeyStyle.SCIENTIFIC)
            b.setOnClickListener { press(pair.second) }
            row3.addView(b, keyLp(index))
        }
        panel.addView(row3)

        val row4 = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("(" to "(", ")" to ")", "1/x" to "1/(", "eˣ" to "exp(", "log₂" to "log2(").forEachIndexed { index, pair ->
            val b = keyButton(pair.first, KeyStyle.SCIENTIFIC)
            b.setOnClickListener { press(pair.second) }
            row4.addView(b, keyLp(index))
        }
        panel.addView(row4)
    }

    private fun keyLp(index: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(0, dp(if (scientific) 37 else 40), 1f).apply { marginStart = if (index == 0) 0 else dp(4) }

    private enum class KeyStyle { NUMBER, FUNCTION, OPERATOR, EQUALS, DANGER, SCIENTIFIC, SCIENTIFIC_MODE }

    private fun keyButton(label: String, style: KeyStyle): MaterialButton {
        val button = MaterialButton(requireContext())
        button.text = label
        button.isAllCaps = false
        button.minWidth = 0
        button.minimumWidth = 0
        button.insetTop = 0
        button.insetBottom = 0
        button.setPadding(0, 0, 0, 0)
        button.cornerRadius = dp(if (style == KeyStyle.SCIENTIFIC) 11 else 15)
        button.setTextColor(ContextCompat.getColor(requireContext(), when (style) {
            KeyStyle.OPERATOR -> R.color.accent
            KeyStyle.EQUALS -> R.color.bg
            KeyStyle.DANGER -> R.color.white
            KeyStyle.SCIENTIFIC_MODE -> R.color.accent
            KeyStyle.SCIENTIFIC -> R.color.text_secondary
            KeyStyle.FUNCTION -> R.color.text_secondary
            else -> R.color.text_primary
        }))
        button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), when (style) {
            KeyStyle.EQUALS -> R.color.accent
            KeyStyle.DANGER -> R.color.rose
            KeyStyle.SCIENTIFIC_MODE -> R.color.card_alt
            else -> R.color.card
        })
        button.strokeWidth = dp(1)
        button.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.border)
        button.textSize = when (style) {
            KeyStyle.OPERATOR -> 19f
            KeyStyle.EQUALS -> 22f
            KeyStyle.SCIENTIFIC -> 11f
            KeyStyle.SCIENTIFIC_MODE -> 10f
            KeyStyle.FUNCTION -> 13f
            else -> 17f
        }
        button.typeface = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_semibold)
        button.stateListAnimator = null
        return button
    }

    private fun press(key: String) {
        if (CalcStore.settings.vibration) binding.root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        CalcSession.press(key, if (scientific) "Científico" else "Básico")
    }

    private fun action(action: String) {
        if (CalcStore.settings.vibration) binding.root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        CalcSession.memoryAction(action)
        updateMeta()
    }

    private fun render() {
        if (_binding == null) return
        binding.expression.text = CalcSession.expression.ifBlank { "0" }
        binding.preview.text = CalcSession.preview
        binding.error.text = CalcSession.error
        binding.error.visibility = if (CalcSession.error.isBlank()) View.GONE else View.VISIBLE
        updateMeta()
    }

    private fun updateMeta() {
        if (_binding == null) return
        binding.angleLabel.text = CalcStore.settings.angleUnit.label
        val memory = CalcStore.memory()
        binding.modeLabel.text = buildString {
            append(if (scientific) "CÁLCULO CIENTÍFICO" else "CÁLCULO BÁSICO")
            if (memory != 0.0) append("  ·  M ${formatShort(memory)}")
        }
        angleKey?.text = CalcStore.settings.angleUnit.label
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun formatShort(value: Double): String =
        com.calculofacil.app.data.formatNumber(value, CalcStore.settings)

    private fun copy() {
        val text = CalcSession.preview.ifBlank { CalcSession.expression }
        if (text.isBlank()) return
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Resultado", text))
        Toast.makeText(requireContext(), "Resultado copiado", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        unsubscribeSession?.invoke(); unsubscribeSession = null
        unsubscribeStore?.invoke(); unsubscribeStore = null
        angleKey = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SCIENTIFIC = "scientific"
        fun newInstance(scientific: Boolean): CalculatorFragment = CalculatorFragment().apply {
            arguments = Bundle().apply { putBoolean(ARG_SCIENTIFIC, scientific) }
        }
    }
}
