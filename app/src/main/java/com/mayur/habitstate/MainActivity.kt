package com.mayur.habitstate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mayur.habitstate.data.theme.ThemeMode
import com.mayur.habitstate.data.theme.ThemePreferencesRepository
import com.mayur.habitstate.presentation.navigation.AppRootNav
import com.mayur.habitstate.ui.theme.HabitStateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeRepo = ThemePreferencesRepository.getInstance(applicationContext)

        setContent {
            val themeMode by themeRepo.themeModeState.collectAsState()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            HabitStateTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRootNav(navController = navController)
                }
            }
        }
    }
}
