//Nama : Aisyah Septiani Indah Rizki
// NIM : 1313623011

package com.example.kalkulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kalkulator.ui.theme.KalkulatorTheme
import net.objecthunter.exp4j.ExpressionBuilder
import java.text.DecimalFormat
import net.objecthunter.exp4j.function.Function

val PinkBackground = Color(0xFFF0E0E0)
val LightPinkButton = Color(0xFFF8E7E7)
val MediumPinkButton = Color(0xFFF2D1D1)
val DarkPinkButton = Color(0xFFE0B4B4)
val AccentPinkButton = Color(0xFFC79292)

val BlackText = Color(0xFF333333)
val WhiteText = Color(0xFFFFFFFF)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KalkulatorTheme {
                CalculatorApp()
            }
        }
    }
}

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

@Composable
fun CalculatorApp(
    modifier: Modifier = Modifier
) {
    var display by rememberSaveable { mutableStateOf("0") }
    var expression by rememberSaveable { mutableStateOf("") }
    var isScientific by rememberSaveable { mutableStateOf(false) }
    var isInv by rememberSaveable { mutableStateOf(false) }

    fun evaluateExpression(exp: String): String {
        return try {
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
            df.format(result).replace(",", ".")
        } catch (e: Exception) {
            "Error"
        }
    }

    val onButtonClick: (String) -> Unit = { buttonText ->
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
                    val result = evaluateExpression(expression)
                    display = result
                    expression = if (result != "Error") result.replace(",", ".") else ""
                }
            }
            "2ⁿᵈ" -> { isInv = !isInv }
            "CalcToggle" -> {
                isScientific = !isScientific
                expression = ""; display = "0"; isInv = false
            }
            // Logika Operasi Standar
            "%", "÷", "×", "-", "+" -> { expression += currentText; display = expression }

            // Tombol Scientific yang harus menambahkan fungsi dan kurung
            "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
            "ln", "log₁₀", "√", "∛x" -> {
                expression += "$currentText("
                display = expression
            }

            // Tombol Scientific tanpa kurung buka
            "(", ")", "mc", "m+", "m-", "mr", "x!", "e", "EE", "Rand", "π", "Deg", "Rad" -> {
                expression += currentText
                display = expression
            }

            // Tombol Scientific yang menambahkan operator
            "x²", "x³", "xʸ", "eˣ", "10ˣ", "1/x" -> {
                expression += when(currentText) {
                    "x²" -> "^2"; "x³" -> "^3"; "xʸ" -> "^"
                    "eˣ" -> "e^"; "10ˣ" -> "10^"; "1/x" -> "1/"
                    else -> ""
                }
                display = expression
            }

            // Angka dan Koma
            else -> {
                val textToAdd = if (buttonText == ",") "." else buttonText
                if (expression == "0" || expression == "Error") {
                    expression = textToAdd
                } else {
                    expression += textToAdd
                }
                display = expression
            }
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
        // 5 Baris Scientific (6 tombol per baris)
        listOf("(", ")", "mc", "m+", "m-", "mr"),
        listOf("2ⁿᵈ", "x²", "x³", "xʸ", "eˣ", "10ˣ"),
        listOf("1/x", "√", "∛x", "ln", "log₁₀", "x!"),
        listOf("Rand", "sin", "cos", "tan", "e", "EE"),
        listOf("sinh", "cosh", "tanh", "π", "Deg", "Rad"),

        // 5 Baris Standar (4 tombol per baris)
        listOf("AC", "±", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("CalcToggle", "0", ",", "=")
    )

    val finalButtonRows = if (isScientific) scientificButtonRows else basicButtonRows

    val spacing = 10.dp


    Box(
        modifier = modifier
            .background(color = PinkBackground) // Latar belakang kalkulator keseluruhan
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .fillMaxSize()
    ) {
        // --- Header Kiri Atas (Teks menu dan Rad) ---
        Text(
            text = "≡",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 12.dp),
            fontSize = 24.sp,
            color = AccentPinkButton, // Warna disesuaikan
            fontWeight = FontWeight.Bold
        )

        if (isScientific) {
            Text(
                text = "Rad",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 40.dp),
                fontSize = 18.sp,
                color = BlackText.copy(alpha = 0.6f), // Teks Rad agar terlihat
                fontWeight = FontWeight.Medium
            )
        }

        // --- Display Area ---
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {

            Text(
                text = display,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                fontSize = if (display.length > 9) 56.sp else 96.sp,
                fontWeight = FontWeight.Light,
                color = BlackText, // Teks display agar terbaca di latar pink
                textAlign = TextAlign.End,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(spacing))

            // Tombol Area
            Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                finalButtonRows.forEachIndexed { rowIndex, rowItems ->

                    val isScientificTopRow = isScientific && rowIndex < 5
                    val currentHeight = if (isScientificTopRow) 42.dp else 76.dp // Tinggi tombol tetap

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        rowItems.forEach { buttonText ->

                            val effectiveWeight = if (buttonText == "0") 2f else 1f
                            val aspectRatioValue = if (buttonText == "0") 2.1f else 1f

                            val buttonModifier = Modifier
                                .weight(effectiveWeight)
                                .height(currentHeight)
                                .aspectRatio(aspectRatioValue)

                            val label = when (buttonText) {
                                "2ⁿᵈ" -> if (isInv) "1ˢᵗ" else "2ⁿᵈ"
                                "CalcToggle" -> "⌕"
                                "∛x" -> "∛x"
                                "xʸ" -> "xʸ"
                                "log₁₀" -> "log₁₀"
                                else -> buttonText
                            }

                            if (buttonText == "0") {
                                Button(
                                    onClick = { onButtonClick(buttonText) },
                                    modifier = buttonModifier,
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkPinkButton,
                                        contentColor = BlackText
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            text = "0",
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(start = 28.dp)
                                        )
                                    }
                                }
                            } else {

                                CalculatorButton(
                                    text = label,
                                    modifier = buttonModifier,
                                    isScientific = isScientificTopRow,
                                    onClick = { onButtonClick(buttonText) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    isScientific: Boolean = false,
    onClick: () -> Unit
) {


    val isLightPink = listOf("mc", "m+", "m-", "mr", "AC", "CE", "%").contains(text) ||
            (isScientific && listOf("(", ")", "2ⁿᵈ", "x²", "x³", "xʸ", "eˣ", "10ˣ", "1/x", "√", "∛x", "ln", "log₁₀", "x!", "Rand", "sin", "cos", "tan", "e", "EE", "sinh", "cosh", "tanh", "π", "Deg", "Rad").contains(text))

    val isMediumPink = listOf("7", "8", "9", "4", "5", "6", "1", "2", "3").contains(text)
    val isDarkPink = listOf("00", ".").contains(text)
    val isAccentPink = listOf("÷", "×", "-", "+", "=").contains(text)


    val (backgroundColor, textColor) = when {
        isLightPink -> LightPinkButton to BlackText
        isMediumPink -> MediumPinkButton to BlackText
        isDarkPink -> DarkPinkButton to BlackText
        isAccentPink -> AccentPinkButton to WhiteText
        else -> MediumPinkButton to BlackText
    }

    val finalContentColor = if (text == "1ˢᵗ") AccentPinkButton else textColor

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = finalContentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(0.dp)
    ) {

        val fontSize = when {
            text.length > 5 && isScientific -> 8.sp
            isScientific && text.length > 2 -> 14.sp
            isScientific -> 16.sp
            text.length > 3 -> 20.sp
            else -> 32.sp
        }
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun CalculatorPreview() {
    KalkulatorTheme {
        CalculatorApp()
    }
}