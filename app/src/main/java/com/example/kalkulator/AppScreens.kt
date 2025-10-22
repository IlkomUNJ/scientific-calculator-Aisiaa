package com.example.kalkulator

sealed class AppScreens(val route: String) {
    object MenuScreen : AppScreens("menu_screen")
    object CalculatorScreen : AppScreens("calculator_screen")
    object TextEditorScreen : AppScreens("text_editor_screen")
}