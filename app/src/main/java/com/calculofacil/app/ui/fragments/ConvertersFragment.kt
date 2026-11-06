package com.calculofacil.app.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.calculofacil.app.R
import com.calculofacil.app.data.CalcSettings
import com.calculofacil.app.data.CalcStore
import com.calculofacil.app.data.Currency
import com.calculofacil.app.data.MeasureUnit
import com.calculofacil.app.data.UnitCategory
import com.calculofacil.app.data.UnitsData
import com.calculofacil.app.data.formatNumber
import com.calculofacil.app.data.parseInputNumber
import com.calculofacil.app.databinding.FragmentConvertersBinding
import com.google.android.material.chip.Chip

class ConvertersFragment : Fragment() {

    private var _binding: FragmentConvertersBinding? = null
    private val binding get() = _binding!!
    private var category = UnitCategory.LENGTH
    private var units: List<MeasureUnit> = emptyList()
    private var currencies: List<Currency> = emptyList()
    private var fromUnit: MeasureUnit? = null
    private var toUnit: MeasureUnit? = null
    private var fromCurrency: Currency? = null
    private var toCurrency: Currency? = null
    private var suppress = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConvertersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        buildCategories()
        binding.valueInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) = recalculate()
        })
        binding.swap.setOnClickListener {
            if (category == UnitCategory.CURRENCY) {
                val tmp = fromCurrency
                fromCurrency = toCurrency
                toCurrency = tmp
                showCurrencySelections()
            } else {
                val tmp = fromUnit
                fromUnit = toUnit
                toUnit = tmp
                showUnitSelections()
            }
            recalculate()
        }
        selectCategory(UnitCategory.LENGTH)
        binding.valueInput.setText("1")
    }

    private fun buildCategories() {
        binding.categoryChips.removeAllViews()
        UnitCategory.entries.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat.title
                isCheckable = true
                isChecked = cat == category
                setTextColor(requireContext().getColor(R.color.text_secondary))
                chipBackgroundColor = requireContext().getColorStateList(R.color.card)
                chipStrokeColor = requireContext().getColorStateList(R.color.border)
                chipStrokeWidth = resources.displayMetrics.density
                chipMinHeight = 32f * resources.displayMetrics.density
                textSize = 10.5f
                setOnClickListener { selectCategory(cat) }
            }
            binding.categoryChips.addView(chip)
        }
    }

    private fun selectCategory(cat: UnitCategory) {
        category = cat
        suppress = true
        if (cat == UnitCategory.CURRENCY) {
            currencies = UnitsData.currencies
            fromCurrency = currencies.firstOrNull { it.code == "BRL" } ?: currencies.first()
            toCurrency = currencies.firstOrNull { it.code == "USD" } ?: currencies.last()
            showCurrencySelections()
            binding.note.visibility = View.VISIBLE
            binding.note.text = "Taxas de referência offline para conversão educativa. Não são cotações ao vivo."
        } else {
            units = UnitsData.units[cat].orEmpty()
            fromUnit = units.firstOrNull()
            toUnit = units.getOrNull(1) ?: units.firstOrNull()
            showUnitSelections()
            binding.note.visibility = View.GONE
        }
        suppress = false
        recalculate()
    }

    private fun showUnitSelections() {
        suppress = true
        val names = units.map { "${it.name} (${it.symbol})" }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, names)
        binding.fromUnit.setAdapter(adapter)
        binding.toUnit.setAdapter(adapter)
        binding.fromUnit.setText(fromUnit?.let { "${it.name} (${it.symbol})" }.orEmpty(), false)
        binding.toUnit.setText(toUnit?.let { "${it.name} (${it.symbol})" }.orEmpty(), false)
        binding.fromUnit.setOnItemClickListener { _, _, position, _ ->
            fromUnit = units.getOrNull(position); recalculate()
        }
        binding.toUnit.setOnItemClickListener { _, _, position, _ ->
            toUnit = units.getOrNull(position); recalculate()
        }
        suppress = false
    }

    private fun showCurrencySelections() {
        suppress = true
        val names = currencies.map { "${it.code} — ${it.name}" }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, names)
        binding.fromUnit.setAdapter(adapter)
        binding.toUnit.setAdapter(adapter)
        binding.fromUnit.setText(fromCurrency?.let { "${it.code} — ${it.name}" }.orEmpty(), false)
        binding.toUnit.setText(toCurrency?.let { "${it.code} — ${it.name}" }.orEmpty(), false)
        binding.fromUnit.setOnItemClickListener { _, _, position, _ ->
            fromCurrency = currencies.getOrNull(position); recalculate()
        }
        binding.toUnit.setOnItemClickListener { _, _, position, _ ->
            toCurrency = currencies.getOrNull(position); recalculate()
        }
        suppress = false
    }

    private fun recalculate() {
        if (suppress || _binding == null) return
        val value = parseInputNumber(binding.valueInput.text?.toString().orEmpty())
        if (value == null) {
            binding.result.text = "—"
            binding.formula.text = "Digite um valor numérico"
            return
        }
        if (category == UnitCategory.CURRENCY) {
            val from = fromCurrency ?: return
            val to = toCurrency ?: return
            val result = UnitsData.convertCurrency(value, from, to)
            binding.result.text = "${to.symbol} ${formatNumber(result, CalcStore.settings)}"
            binding.formula.text = "1 ${from.code} = ${formatNumber(to.rateToUsd / from.rateToUsd, CalcStore.settings)} ${to.code}"
            return
        }
        val from = fromUnit ?: return
        val to = toUnit ?: return
        val result = UnitsData.convert(value, from, to, category)
        binding.result.text = "${formatNumber(result, CalcStore.settings)} ${to.symbol}"
        binding.formula.text = if (category == UnitCategory.TEMPERATURE) {
            "${from.symbol} → ${to.symbol}"
        } else {
            "1 ${from.symbol} = ${formatNumber(from.factor / to.factor, CalcStore.settings)} ${to.symbol}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
