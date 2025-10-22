package com.example.kalkulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

val MenuBackground = Color(0xFFF0E0E0)
val MenuButtonColor = Color(0xFFC79292)
val MenuTextColor = Color(0xFFFFFFFF)

@Composable
fun MenuScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MenuBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Menu Utama",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Tombol Kalkulator Ilmiah
        MenuNavigationButton(
            text = "Kalkulator Ilmiah",
            onClick = { navController.navigate(AppScreens.CalculatorScreen.route) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Text Editor (Notepad)
        MenuNavigationButton(
            text = "Text Editor (Notepad)",
            onClick = { navController.navigate(AppScreens.TextEditorScreen.route) }
        )
    }
}

@Composable
fun MenuNavigationButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MenuButtonColor,
            contentColor = MenuTextColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}