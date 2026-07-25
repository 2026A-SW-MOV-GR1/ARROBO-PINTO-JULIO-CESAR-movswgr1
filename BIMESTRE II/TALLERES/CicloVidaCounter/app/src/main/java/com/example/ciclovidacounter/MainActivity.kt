package com.example.ciclovidacounter

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvCount: TextView

    private var count: Int = 0

    companion object {

        /*
         * false: permite demostrar que el contador se reinicia.
         * true: activa la persistencia y mantiene el contador al rotar.
         */
        private const val USE_PERSISTENCE = false

        private const val TAG = "CICLO_VIDA"
        private const val KEY_COUNT = "KEY_COUNT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        tvCount = findViewById(R.id.tvCount)
        val btnIncrement: Button = findViewById(R.id.btnIncrement)

        count = if (USE_PERSISTENCE) {
            savedInstanceState?.getInt(KEY_COUNT, 0) ?: 0
        } else {
            0
        }

        updateCounter()

        btnIncrement.setOnClickListener {
            count++
            updateCounter()

            Log.d(
                TAG,
                "BOTÓN +1 | count=$count | instancia=${System.identityHashCode(this)}"
            )
        }

        logLifecycle(
            "onCreate | Bundle recibido=${savedInstanceState != null}"
        )
    }

    override fun onStart() {
        super.onStart()
        logLifecycle("onStart")
    }

    override fun onResume() {
        super.onResume()
        logLifecycle("onResume")
    }

    override fun onPause() {
        logLifecycle("onPause")
        super.onPause()
    }

    override fun onStop() {
        logLifecycle("onStop")
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        logLifecycle("onRestart")
    }

    override fun onDestroy() {
        Log.d(
            TAG,
            "onDestroy | count=$count" +
                    " | cambioConfiguracion=$isChangingConfigurations" +
                    " | finalizando=$isFinishing" +
                    " | instancia=${System.identityHashCode(this)}"
        )

        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {

        if (USE_PERSISTENCE) {
            outState.putInt(KEY_COUNT, count)
        }

        logLifecycle("onSaveInstanceState | guardando count=$count")

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        if (USE_PERSISTENCE) {
            count = savedInstanceState.getInt(KEY_COUNT, 0)
        } else {
            count = 0
        }

        updateCounter()

        logLifecycle("onRestoreInstanceState | recuperando count=$count")
    }

    private fun updateCounter() {
        tvCount.text = getString(R.string.count_format, count)
    }

    private fun logLifecycle(event: String) {
        Log.d(
            TAG,
            "$event | count=$count | instancia=${System.identityHashCode(this)}"
        )
    }
}