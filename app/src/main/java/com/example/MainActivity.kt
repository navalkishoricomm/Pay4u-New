package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.Pay4uApp
import com.example.ui.theme.RechargePayTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    com.example.data.network.ServerConfig.init(this)
    setContent {
      RechargePayTheme {
        Pay4uApp()
      }
    }
  }
}
