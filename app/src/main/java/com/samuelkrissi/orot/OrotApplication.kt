package com.samuelkrissi.orot

import android.app.Application
import com.samuelkrissi.orot.data.AppContainer

class OrotApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
