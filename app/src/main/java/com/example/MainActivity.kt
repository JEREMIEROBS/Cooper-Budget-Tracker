package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.db.BudgetDatabase
import com.example.data.repository.BudgetRepository
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val database = BudgetDatabase.getDatabase(applicationContext)
        val repository = BudgetRepository(database)
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDarkMode = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainScreen(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}
