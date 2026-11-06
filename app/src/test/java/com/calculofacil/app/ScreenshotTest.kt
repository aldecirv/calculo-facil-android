package com.calculofacil.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.calculofacil.app.data.CalcHistory
import com.calculofacil.app.data.CalcSession
import com.calculofacil.app.data.CalcStore
import com.calculofacil.app.ui.MainActivity
import com.calculofacil.app.ui.SettingsActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w411dp-h891dp-xxhdpi")
class ScreenshotTest {
    private val dir = File("build/screenshots").apply { mkdirs() }

    @Test fun captureScreens() {
        val context = ApplicationProvider.getApplicationContext<CalculoFacilApplication>()
        CalcStore.init(context)
        CalcStore.resetForTests(context)
        CalcSession.press("2", "Básico")
        CalcSession.press("+", "Básico")
        CalcSession.press("3", "Básico")
        CalcSession.press("×", "Básico")
        CalcSession.press("4", "Básico")
        CalcSession.press("=", "Básico")
        CalcStore.addHistory(CalcHistory(expression = "2+3×4", result = "14", category = "Básico"))

        val main = Robolectric.buildActivity(MainActivity::class.java).setup().visible().get()
        idle(main)
        capture(main.window.decorView, "50_basic")

        main.openTab(R.id.navScientific)
        idle(main)
        capture(main.window.decorView, "51_scientific")

        main.openTab(R.id.navConverters)
        idle(main)
        capture(main.window.decorView, "52_converters")

        main.openTab(R.id.navFinance)
        idle(main)
        capture(main.window.decorView, "53_finance")

        main.openTab(R.id.navHistory)
        idle(main)
        capture(main.window.decorView, "54_history")

        val settings = Robolectric.buildActivity(SettingsActivity::class.java).setup().visible().get()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        capture(settings.window.decorView, "55_settings")
    }

    private fun idle(activity: MainActivity) {
        activity.supportFragmentManager.executePendingTransactions()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    private fun capture(view: View, name: String) {
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(2280, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, 1080, 2280)
        val bitmap = Bitmap.createBitmap(1080, 2280, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
