package com.calculofacil.app

import android.os.Looper
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ApplicationProvider
import com.calculofacil.app.data.CalcStore
import com.calculofacil.app.ui.MainActivity
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
class FinanceToolsTest {
    @Test fun allToolsOpen() {
        val context = ApplicationProvider.getApplicationContext<CalculoFacilApplication>()
        CalcStore.init(context)
        CalcStore.resetForTests(context)
        val main = Robolectric.buildActivity(MainActivity::class.java).setup().visible().get()
        main.openTab(R.id.navFinance)
        main.supportFragmentManager.executePendingTransactions()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        val tools = main.findViewById<LinearLayout>(R.id.toolsContainer)
        assertEquals(6, tools.childCount)
        for (i in 0 until tools.childCount) {
            tools.getChildAt(i).performClick()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            val dialog = ShadowDialog.getLatestDialog() as? AlertDialog
            assertNotNull("Ferramenta $i não abriu", dialog)
            dialog?.dismiss()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
