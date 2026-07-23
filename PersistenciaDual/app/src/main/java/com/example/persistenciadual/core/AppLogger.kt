package com.example.persistenciadual.core

import android.util.Log

interface AppLogger {

    fun debug(tag: String, message: String)

    fun info(tag: String, message: String)

    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null
    )
}

class AndroidAppLogger : AppLogger {

    override fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun info(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun error(
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        if (throwable == null) {
            Log.e(tag, message)
        } else {
            Log.e(tag, message, throwable)
        }
    }
}