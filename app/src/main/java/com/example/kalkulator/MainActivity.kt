//Nama : Aisyah Septiani Indah Rizki
// NIM : 1313623011

package com.example.kalkulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kalkulator.ui.theme.KalkulatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            MainAppNavigation()
        }
    }
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreens.MenuScreen.route
    ) {
        composable(AppScreens.MenuScreen.route) {
            MenuScreen(navController = navController)
        }

        composable(AppScreens.CalculatorScreen.route) {
            KalkulatorTheme {
                CalculatorApp()
            }
        }

        composable(route = AppScreens.TextEditorScreen.route) {
            TextEditorScreen(navController = navController)
        }
    }
}