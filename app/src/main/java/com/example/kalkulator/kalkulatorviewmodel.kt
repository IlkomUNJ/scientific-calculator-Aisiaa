package com.example.kalkulator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class KalkulatorViewModel : ViewModel() {
    var display by mutableStateOf("0")
        private set
    var expression by mutableStateOf("")
        private set
    var isScientific by mutableStateOf(false)
        private set
    var isInv by mutableStateOf(false)
        private set

    val finalButtonRows: List<List<String>>
        get() = if (isScientific) scientificButtonRows else basicButtonRows


    fun onButtonClick(buttonText: String) {
        var currentText = buttonText

        if (isInv) {
            currentText = when (buttonText) {
                "sin" -> "asin"; "cos" -> "acos"; "tan" -> "atan"
                "sinh" -> "asinh"; "cosh" -> "acosh"; "tanh" -> "atanh"
                else -> buttonText
            }
        }

        when (currentText) {
            "AC" -> { expression = ""; display = "0"; isInv = false }
            "±" -> {
                expression = if (expression.startsWith("-")) expression.removePrefix("-") else "-$expression"
                display = expression
            }
            "=" -> {
                if (expression.isNotEmpty()) {
                    val result = evaluateExpression(expression) // Memanggil fungsi dari KalkulatorModel.kt
                    display = result
                    expression = if (result != "Error") result.replace(",", ".") else ""
                }
            }
            "2ⁿᵈ" -> { isInv = !isInv }
            "CalcToggle" -> {
                isScientific = !isScientific
                expression = ""; display = "0"; isInv = false
            }
            "%", "÷", "×", "-", "+" -> { expression += currentText; display = expression }

            "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
            "ln", "log₁₀", "√", "∛x" -> {
                expression += "$currentText("
                display = expression
            }

            "(", ")", "mc", "m+", "m-", "mr", "x!", "e", "EE", "Rand", "π", "Deg", "Rad" -> {
                expression += currentText
                display = expression
            }

            "x²", "x³", "xʸ", "eˣ", "10ˣ", "1/x" -> {
                expression += when(currentText) {
                    "x²" -> "^2"; "x³" -> "^3"; "xʸ" -> "^"
                    "eˣ" -> "e^"; "10ˣ" -> "10^"; "1/x" -> "1/"
                    else -> ""
                }
                display = expression
            }

            else -> {
                val textToAdd = if (buttonText == ",") "." else buttonText
                if (expression.isEmpty() || expression == "0" || display == "Error") {
                    expression = textToAdd
                } else {
                    expression += textToAdd
                }
                display = expression
            }
        }
    }
}