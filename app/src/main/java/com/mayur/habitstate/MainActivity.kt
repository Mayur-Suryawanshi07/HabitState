package com.mayur.habitstate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mayur.habitstate.presentation.navigation.AppRootNav
import com.mayur.habitstate.ui.theme.HabitStateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitStateTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppRootNav(navController = navController)
                }
            }
        }
    }
}
