package com.calculofacil.app

import androidx.appcompat.app.AlertDialog
import android.os.Looper
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.calculofacil.app.data.CalcStore
import com.calculofacil.app.ui.MainActivity
import com.google.android.material.textfield.TextInputEditText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w411dp-h891dp-xxhdpi")
class FinanceFlowTest {
    @Test fun discountDialogCalculatesAndSaves() {
        val context = ApplicationProvider.getApplicationContext<CalculoFacilApplication>()
        CalcStore.init(context)
        CalcStore.resetForTests(context)
        val main = Robolectric.buildActivity(MainActivity::class.java).setup().visible().get()
        main.openTab(R.id.navFinance)
        main.supportFragmentManager.executePendingTransactions()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        val tools = main.findViewById<LinearLayout>(R.id.toolsContainer)
        assertEquals(6, tools.childCount)
        tools.getChildAt(0).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        val dialog = ShadowDialog.getLatestDialog() as? AlertDialog
        assertNotNull(dialog)
        val alert = dialog!!
        val edits = mutableListOf<TextInputEditText>()
        fun collect(view: android.view.View) {
            if (view is TextInputEditText) edits.add(view)
            if (view is android.view.ViewGroup) for (i in 0 until view.childCount) collect(view.getChildAt(i))
        }
        collect(alert.window!!.decorView)
        edits[0].setText("100")
        edits[1].setText("20")
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, CalcStore.history().size)
        assertEquals("Financeiro", CalcStore.history().first().category)
    }
}
