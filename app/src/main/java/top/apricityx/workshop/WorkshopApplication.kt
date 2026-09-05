package top.apricityx.workshop

import android.app.Application

class WorkshopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        AppRuntimeLogManager.initialize(this)
    }

    companion object {
        lateinit var instance: WorkshopApplication
            private set
    }
}