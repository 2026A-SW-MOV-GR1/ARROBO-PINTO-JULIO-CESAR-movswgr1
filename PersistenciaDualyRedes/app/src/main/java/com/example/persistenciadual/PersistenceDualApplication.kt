package com.example.persistenciadual

import android.app.Application
import com.example.persistenciadual.di.AppContainer

class PersistenceDualApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(
            context = applicationContext
        )
    }
}