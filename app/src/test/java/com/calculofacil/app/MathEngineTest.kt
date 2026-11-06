package com.calculofacil.app

import com.calculofacil.app.data.AngleUnit
import com.calculofacil.app.data.MathEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathEngineTest {
    private fun eval(expr: String, angle: AngleUnit = AngleUnit.DEG): Double {
        val result = MathEngine.evaluate(expr, angle)
        assertTrue("Erro em $expr: $result", result is MathEngine.EvalResult.Success)
        return (result as MathEngine.EvalResult.Success).value
    }

    @Test fun precedence() {
        assertEquals(14.0, eval("2+3*4"), 0.000001)
        assertEquals(20.0, eval("(2+3)*4"), 0.000001)
        assertEquals(512.0, eval("2^3^2"), 0.000001)
    }

    @Test fun functionsAndConstants() {
        assertEquals(0.5, eval("sin(30)"), 0.000001)
        assertEquals(2.0, eval("sqrt(4)"), 0.000001)
        assertEquals(120.0, eval("5!"), 0.000001)
        assertEquals(0.1, eval("10%"), 0.000001)
        assertEquals(6.0, eval("2(1+2)"), 0.000001)
        assertEquals(-1.0, eval("cos(180)"), 0.000001)
    }

    @Test fun radians() {
        assertEquals(1.0, eval("sin(pi/2)", AngleUnit.RAD), 0.000001)
    }

    @Test fun errorsAreExplicit() {
        assertTrue(MathEngine.evaluate("1/0", AngleUnit.DEG) is MathEngine.EvalResult.Error)
        assertTrue(MathEngine.evaluate("sqrt(-1)", AngleUnit.DEG) is MathEngine.EvalResult.Error)
        assertTrue(MathEngine.evaluate("abc", AngleUnit.DEG) is MathEngine.EvalResult.Error)
    }
}
