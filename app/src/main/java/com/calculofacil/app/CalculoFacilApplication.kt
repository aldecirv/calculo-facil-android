package com.calculofacil.app

import android.app.Application
import com.calculofacil.app.data.CalcStore

class CalculoFacilApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CalcStore.init(this)
    }
}
