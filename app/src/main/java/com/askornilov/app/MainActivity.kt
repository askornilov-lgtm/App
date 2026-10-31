package com.askornilov.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.askornilov.app.ui.AppRoot
import com.askornilov.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = (application as App).db
        setContent {
            AppTheme {
                val vm: MainViewModel = viewModel(
                    factory = viewModelFactory { initializer { MainViewModel(db) } },
                )
                AppRoot(vm)
            }
        }
    }
}
