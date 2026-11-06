package com.calculofacil.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object CalcStore {

    private const val PREFS = "calculo_facil_prefs"
    private const val KEY_HISTORY = "history"
    private const val KEY_MEMORY = "memory"
    private const val KEY_ANGLE = "angle"
    private const val KEY_FORMAT = "format"
    private const val KEY_PRECISION = "precision"
    private const val KEY_VIBRATION = "vibration"

    private lateinit var prefs: SharedPreferences
    var settings = CalcSettings()
        private set
    private var historyCache: MutableList<CalcHistory> = mutableListOf()
    private var listeners = mutableListOf<() -> Unit>()

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        settings = CalcSettings(
            angleUnit = if (prefs.getString(KEY_ANGLE, "DEG") == "RAD") AngleUnit.RAD else AngleUnit.DEG,
            numberFormat = if (prefs.getString(KEY_FORMAT, "pt-BR") == "en-US") NumberFormatKind.EN_US else NumberFormatKind.PT_BR,
            precision = prefs.getInt(KEY_PRECISION, 0),
            vibration = prefs.getBoolean(KEY_VIBRATION, true)
        )
        historyCache = loadHistory()
    }

    fun updateSettings(block: (CalcSettings) -> Unit) {
        block(settings)
        prefs.edit()
            .putString(KEY_ANGLE, settings.angleUnit.name)
            .putString(KEY_FORMAT, if (settings.numberFormat == NumberFormatKind.PT_BR) "pt-BR" else "en-US")
            .putInt(KEY_PRECISION, settings.precision)
            .putBoolean(KEY_VIBRATION, settings.vibration)
            .commit()
        CalcSession.refresh()
        notifyListeners()
    }

    fun history(): List<CalcHistory> = historyCache.toList()

    fun addHistory(item: CalcHistory) {
        historyCache.add(0, item)
        while (historyCache.size > 100) historyCache.removeAt(historyCache.size - 1)
        saveHistory()
        notifyListeners()
    }

    fun clearHistory() {
        historyCache.clear()
        saveHistory()
        notifyListeners()
    }

    fun memory(): Double = prefs.getFloat(KEY_MEMORY, 0f).toDouble()

    fun setMemory(value: Double) {
        prefs.edit().putFloat(KEY_MEMORY, value.toFloat()).commit()
        notifyListeners()
    }

    fun subscribe(listener: () -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener); Unit }
    }

    private fun notifyListeners() {
        listeners.toList().forEach { it() }
    }

    private fun saveHistory() {
        val array = JSONArray()
        historyCache.forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("expression", it.expression)
                put("result", it.result)
                put("timestamp", it.timestamp)
                put("category", it.category)
                put("label", it.label)
            })
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).commit()
    }

    private fun loadHistory(): MutableList<CalcHistory> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return mutableListOf()
        return runCatching {
            val array = JSONArray(raw)
            val list = mutableListOf<CalcHistory>()
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                list.add(
                    CalcHistory(
                        id = o.optString("id"),
                        expression = o.optString("expression"),
                        result = o.optString("result"),
                        timestamp = o.optLong("timestamp"),
                        category = o.optString("category"),
                        label = o.optString("label")
                    )
                )
            }
            list
        }.getOrDefault(mutableListOf())
    }

    fun resetForTests(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        settings = CalcSettings()
        historyCache = mutableListOf()
        CalcSession.clear()
    }
}
