package com.calculofacil.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.calculofacil.app.R
import com.calculofacil.app.data.CalcSession
import com.calculofacil.app.databinding.ActivityMainBinding
import com.calculofacil.app.ui.fragments.CalculatorFragment
import com.calculofacil.app.ui.fragments.ConvertersFragment
import com.calculofacil.app.ui.fragments.FinanceFragment
import com.calculofacil.app.ui.fragments.HistoryFragment
import com.calculofacil.app.util.ThemeUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentTab = R.id.navBasic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.darkSystemBars(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }
        binding.settingsButton.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.navBasic.setOnClickListener { openTab(R.id.navBasic) }
        binding.navScientific.setOnClickListener { openTab(R.id.navScientific) }
        binding.navConverters.setOnClickListener { openTab(R.id.navConverters) }
        binding.navFinance.setOnClickListener { openTab(R.id.navFinance) }
        binding.navHistory.setOnClickListener { openTab(R.id.navHistory) }
        if (savedInstanceState == null) openTab(R.id.navBasic)
        else currentTab = savedInstanceState.getInt("tab", R.id.navBasic).also { openTab(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("tab", currentTab)
        super.onSaveInstanceState(outState)
    }

    fun openTab(tabId: Int) {
        currentTab = tabId
        val fragment: Fragment = when (tabId) {
            R.id.navScientific -> CalculatorFragment.newInstance(scientific = true)
            R.id.navConverters -> ConvertersFragment()
            R.id.navFinance -> FinanceFragment()
            R.id.navHistory -> HistoryFragment()
            else -> CalculatorFragment.newInstance(scientific = false)
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
        updateNav(tabId)
    }

    fun openCalculator(expression: String, scientific: Boolean) {
        CalcSession.setExpression(expression)
        openTab(if (scientific) R.id.navScientific else R.id.navBasic)
    }

    private fun updateNav(selected: Int) {
        val items = mapOf(
            R.id.navBasic to Triple(binding.iconBasic, binding.labelBasic, "Básica"),
            R.id.navScientific to Triple(binding.iconScientific, binding.labelScientific, "Científica"),
            R.id.navConverters to Triple(binding.iconConverters, binding.labelConverters, "Conversores"),
            R.id.navFinance to Triple(binding.iconFinance, binding.labelFinance, "Financeiro"),
            R.id.navHistory to Triple(binding.iconHistory, binding.labelHistory, "Histórico")
        )
        items.forEach { (id, triple) ->
            val active = id == selected
            triple.first.setColorFilter(Color.parseColor(if (active) "#10B981" else "#8FA3C0"))
            triple.second.setTextColor(Color.parseColor(if (active) "#10B981" else "#8FA3C0"))
            val parent = triple.first.parent as android.view.View
            parent.setBackgroundResource(if (active) R.drawable.bg_nav_active else android.R.color.transparent)
        }
    }
}
