package com.setupassistant.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.setupassistant.app.ui.AppScaffold
import com.setupassistant.app.ui.AuthGate
import com.setupassistant.app.ui.theme.SetupAssistantTheme

// BiometricPrompt が FragmentActivity を要求するため ComponentActivity ではない
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetupAssistantTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthGate {
                        AppScaffold()
                    }
                }
            }
        }
    }
}
