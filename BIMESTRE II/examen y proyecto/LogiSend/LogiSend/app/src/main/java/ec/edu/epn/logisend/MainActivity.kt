package ec.edu.epn.logisend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ec.edu.epn.logisend.ui.LogiSendScreen
import ec.edu.epn.logisend.ui.theme.LogiSendTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        enableEdgeToEdge()

        setContent {

            LogiSendTheme {

                LogiSendScreen()
            }
        }
    }
}