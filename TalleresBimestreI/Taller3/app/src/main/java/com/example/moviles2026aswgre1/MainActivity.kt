package com.example.moviles2026aswgre1

import android.content.res.Configuration
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = findViewByIdAfterSetContent()
        cargarContenidoWeb()
    }

    private fun findViewByIdAfterSetContent(): WebView {
        setContentView(R.layout.activity_main)
        return findViewById(R.id.webView)
    }

    private fun cargarContenidoWeb() {
        val texto = getString(R.string.dynamic_text)
        val colorTexto = obtenerColorHex(R.color.color_texto)
        val colorFondo = obtenerColorHex(R.color.color_fondo)

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        margin: 0;
                        width: 100vw;
                        height: 100vh;
                        background-color: $colorFondo;
                        color: $colorTexto;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        font-family: Arial, sans-serif;
                    }

                    .card {
                        text-align: center;
                        padding: 30px;
                        border-radius: 20px;
                    }

                    h1 {
                        font-size: 32px;
                    }

                    p {
                        font-size: 18px;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <h1>$texto</h1>
                    <p>Aplicación WebView con recursos nativos Android</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun obtenerColorHex(colorRes: Int): String {
        val color = ContextCompat.getColor(this, colorRes)
        return String.format("#%06X", 0xFFFFFF and color)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        cargarContenidoWeb()
    }
}