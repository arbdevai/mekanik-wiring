package com.mekanik.wiring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekanik.wiring.data.local.WiringDatabase
import com.mekanik.wiring.data.repository.WiringRepositoryImpl
import com.mekanik.wiring.data.updater.UpdateManager
import com.mekanik.wiring.ui.WiringScreen
import com.mekanik.wiring.ui.theme.MekanikWiringTheme
import com.mekanik.wiring.ui.viewmodel.WiringViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = WiringDatabase.getInstance(applicationContext)
        val repository = WiringRepositoryImpl(database.diagramDao())
        val updateManager = UpdateManager(applicationContext)

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WiringViewModel(repository, updateManager) as T
            }
        }
        val viewModel = ViewModelProvider(this, viewModelFactory)[WiringViewModel::class.java]

        setContent {
            MekanikWiringTheme {
                WiringScreen(viewModel = viewModel)
            }
        }
    }
}
