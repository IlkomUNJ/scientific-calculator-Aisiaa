package com.example.kalkulator

import androidx.compose.ui.graphics.Color
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import java.text.DecimalFormat

val PinkBackground = Color(0xFFF0E0E0)
val LightPinkButton = Color(0xFFF8E7E7)
val MediumPinkButton = Color(0xFFF2D1D1)
val DarkPinkButton = Color(0xFFE0B4B4)
val AccentPinkButton = Color(0xFFC79292)

val BlackText = Color(0xFF333333)
val WhiteText = Color(0xFFFFFFFF)

val factorial = object : Function("fact", 1) {
    override fun apply(vararg args: Double): Double {
        val n = args[0].toInt()
        if (n < 0 || n != args[0].toInt()) {
            throw IllegalArgumentException("Argument for factorial must be a non-negative integer")
        }
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }
}

fun evaluateExpression(expression: String): String {
    return try {
        // Logika sanitasi ekspresi persis seperti kode asli Anda
        val sanitizedExp = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("√", "sqrt")
            .replace("π", Math.PI.toString())
            .replace("ln", "log")
            .replace("log₁₀", "log10")
            .replace("x²", "^2")
            .replace("x³", "^3")
            .replace("xʸ", "^")
            .replace("eˣ", "e^")
            .replace("10ˣ", "10^")
            .replace("EE", "E")

        val expressionBuilder = ExpressionBuilder(sanitizedExp)
            .function(factorial)
            .build()

        val result = expressionBuilder.evaluate()
        val df = DecimalFormat("#.#########")
        // Mengganti koma menjadi titik untuk konsistensi notasi
        df.format(result).replace(",", ".")
    } catch (e: Exception) {
        "Error"
    }
}

val basicButtonRows = listOf(
    listOf("AC", "±", "%", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "-"),
    listOf("1", "2", "3", "+"),
    listOf("CalcToggle", "0", ",", "=")
)

val scientificButtonRows = listOf(
    listOf("(", ")", "mc", "m+", "m-", "mr"),
    listOf("2ⁿᵈ", "x²", "x³", "xʸ", "eˣ", "10ˣ"),
    listOf("1/x", "√", "∛x", "ln", "log₁₀", "x!"),
    listOf("Rand", "sin", "cos", "tan", "e", "EE"),
    listOf("sinh", "cosh", "tanh", "π", "Deg", "Rad"),

    listOf("AC", "±", "%", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "-"),
    listOf("1", "2", "3", "+"),
    listOf("CalcToggle", "0", ",", "=")
)