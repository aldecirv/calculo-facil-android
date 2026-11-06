package com.calculofacil.app

import androidx.test.core.app.ApplicationProvider
import com.calculofacil.app.data.CalcHistory
import com.calculofacil.app.data.CalcStore
import com.calculofacil.app.data.CalcSettings
import com.calculofacil.app.data.formatNumber
import com.calculofacil.app.data.parseInputNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CalcStoreTest {
    private val context = ApplicationProvider.getApplicationContext<CalculoFacilApplication>()

    @Before fun setup() {
        CalcStore.init(context)
        CalcStore.resetForTests(context)
    }

    @Test fun parsesDecimalConventions() {
        assertEquals(1234.56, parseInputNumber("1.234,56") ?: 0.0, 0.0001)
        assertEquals(3.14, parseInputNumber("3.14") ?: 0.0, 0.0001)
        assertEquals(1234.0, parseInputNumber("1.234") ?: 0.0, 0.0001)
        assertEquals(null, parseInputNumber(""))
    }

    @Test fun formatsNumbers() {
        val pt = CalcSettings()
        assertEquals("1.234,5", formatNumber(1234.5, pt))
        assertEquals("R$ 10,00", "R$ " + formatNumber(10.0, pt.copy(precision = 2)))
    }

    @Test fun storesHistoryAndMemory() {
        CalcStore.addHistory(CalcHistory(expression = "2+2", result = "4", category = "Básico"))
        assertEquals(1, CalcStore.history().size)
        assertEquals("2+2", CalcStore.history().first().expression)
        CalcStore.setMemory(15.5)
        assertEquals(15.5, CalcStore.memory(), 0.001)
        assertNotNull(CalcStore.history().first().id)
        CalcStore.clearHistory()
        assertEquals(0, CalcStore.history().size)
    }
}
