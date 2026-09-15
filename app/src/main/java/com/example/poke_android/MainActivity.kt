package com.example.poke_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.poke_android.ui.PokeApp
import com.example.poke_android.ui.PokeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PokeApplication
        val factory = viewModelFactory {
            initializer { PokeViewModel(app.repository, app.session, app.unauthorized) }
        }
        setContent { PokeApp(viewModel(factory = factory)) }
    }
}
