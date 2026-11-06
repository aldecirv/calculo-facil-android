package com.calculofacil.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.calculofacil.app.R
import com.calculofacil.app.data.AngleUnit
import com.calculofacil.app.data.CalcSettings
import com.calculofacil.app.data.CalcStore
import com.calculofacil.app.data.NumberFormatKind
import com.calculofacil.app.databinding.ActivitySettingsBinding
import com.calculofacil.app.util.ThemeUtils

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.darkSystemBars(this)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }
        binding.back.setOnClickListener { finish() }
        load()
        binding.angleGroup.setOnCheckedChangeListener { _, id ->
            CalcStore.updateSettings { it.angleUnit = if (id == R.id.angleRad) AngleUnit.RAD else AngleUnit.DEG }
        }
        binding.formatGroup.setOnCheckedChangeListener { _, id ->
            CalcStore.updateSettings { it.numberFormat = if (id == R.id.formatUs) NumberFormatKind.EN_US else NumberFormatKind.PT_BR }
        }
        binding.precisionGroup.setOnCheckedStateChangeListener { _, ids ->
            val precision = when (ids.firstOrNull()) {
                R.id.precision2 -> 2
                R.id.precision4 -> 4
                R.id.precision6 -> 6
                R.id.precision8 -> 8
                else -> 0
            }
            CalcStore.updateSettings { it.precision = precision }
        }
        binding.vibrationSwitch.setOnCheckedChangeListener { _, checked ->
            CalcStore.updateSettings { it.vibration = checked }
        }
    }

    private fun load() {
        binding.angleDeg.isChecked = CalcStore.settings.angleUnit == AngleUnit.DEG
        binding.angleRad.isChecked = CalcStore.settings.angleUnit == AngleUnit.RAD
        binding.formatBr.isChecked = CalcStore.settings.numberFormat == NumberFormatKind.PT_BR
        binding.formatUs.isChecked = CalcStore.settings.numberFormat == NumberFormatKind.EN_US
        when (CalcStore.settings.precision) {
            2 -> binding.precision2.isChecked = true
            4 -> binding.precision4.isChecked = true
            6 -> binding.precision6.isChecked = true
            8 -> binding.precision8.isChecked = true
            else -> binding.precisionAuto.isChecked = true
        }
        binding.vibrationSwitch.isChecked = CalcStore.settings.vibration
    }
}
