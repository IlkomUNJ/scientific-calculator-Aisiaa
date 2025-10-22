package com.example.kalkulator

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kalkulator.ui.theme.KalkulatorTheme

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

@Composable
fun CalculatorApp(
    modifier: Modifier = Modifier,

    viewModel: KalkulatorViewModel = viewModel()
) {
    val spacing = 10.dp

    Box(
        modifier = modifier
            .background(color = PinkBackground)
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "≡",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 12.dp),
            fontSize = 24.sp,
            color = AccentPinkButton,
            fontWeight = FontWeight.Bold
        )

        if (viewModel.isScientific) {
            Text(
                text = "Rad",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 40.dp),
                fontSize = 18.sp,
                color = BlackText.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {

            Text(
                text = viewModel.display,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                fontSize = if (viewModel.display.length > 9) 56.sp else 96.sp,
                fontWeight = FontWeight.Light,
                color = BlackText,
                textAlign = TextAlign.End,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(spacing))

            Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                viewModel.finalButtonRows.forEachIndexed { rowIndex, rowItems ->

                    val isScientificTopRow = viewModel.isScientific && rowIndex < 5
                    val currentHeight = if (isScientificTopRow) 42.dp else 76.dp

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
                                "2ⁿᵈ" -> if (viewModel.isInv) "1ˢᵗ" else "2ⁿᵈ"
                                "CalcToggle" -> "⌕"
                                "∛x" -> "∛x"
                                "xʸ" -> "xʸ"
                                "log₁₀" -> "log₁₀"
                                else -> buttonText
                            }

                            if (buttonText == "0") {
                                Button(
                                    onClick = { viewModel.onButtonClick(buttonText) },
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
                                    onClick = { viewModel.onButtonClick(buttonText) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun CalculatorPreview() {
    KalkulatorTheme {
        CalculatorApp()
    }
}