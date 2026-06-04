package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ShopRepository
import com.example.ui.ShopDashboardScreen
import com.example.ui.ShopViewModel
import com.example.ui.ShopViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Database, DAO and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = ShopRepository(database.shopDao())
        
        // Instantiate the ViewModel using the Custom Factory
        val viewModel: ShopViewModel by viewModels {
            ShopViewModelFactory(repository)
        }
        
        // Enable modern Edge-to-Edge interface support
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShopDashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
