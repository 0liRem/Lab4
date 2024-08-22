package com.example.lab4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorUI()
        }
    }
}

@Composable
fun CalculatorUI() {
    var displayText by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Text(
                text = displayText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            val buttonSpacing = 8.dp

            Column(modifier = Modifier.fillMaxWidth()) {
                val buttons = listOf(
                    listOf("^", "√", "RESET"),
                    listOf("(", ")", "/", "E"),
                    listOf("7", "8", "9", "*"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf(".", "0", "=", "←")
                )

                buttons.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = buttonSpacing)
                    ) {
                        row.forEach { label ->
                            CalculatorButton(
                                label = label,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            ) {
                                when (label) {
                                    "RESET" -> displayText = ""
                                    "←" -> if (displayText.isNotEmpty()) displayText = displayText.dropLast(1)
                                    "=" -> displayText = evaluateExpression(displayText)
                                    else -> displayText += label
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = label, fontSize = 24.sp, color = Color.White)
    }
}

fun evaluateExpression(expression: String): String {
    val tokens = ArrayList<String>()
    for (i in expression) {
        tokens.add(i.toString())
    }
    return calculadora(tokens)
}

fun calculadora(tokens: ArrayList<String>): String {
    val stack = ArrayDeque<Double>()
    val operators = setOf("+", "-", "*", "/", "^", "r", "e")
    val postfix = ArrayDeque<String>()
    val opStack = ArrayDeque<String>()
    val precedence = mapOf(
        "+" to 1, "-" to 1, "*" to 2, "/" to 2, "^" to 3, "r" to 3, "e" to 3
    )

    // Eliminar espacios y filtrar tokens vacíos
    val cleanedTokens = tokens.map { it.trim() }.filter { it.isNotEmpty() }

    fun toPostfix() {
        for (token in cleanedTokens) {
            when {
                token.toDoubleOrNull() != null -> postfix.add(token)
                token == "(" -> opStack.add(token)
                token == ")" -> {
                    while (opStack.isNotEmpty() && opStack.last() != "(") {
                        postfix.add(opStack.removeLast())
                    }
                    opStack.removeLast()
                }
                token in operators -> {
                    while (opStack.isNotEmpty() && precedence[opStack.last()] ?: 0 >= (precedence[token] ?: 0)) {
                        postfix.add(opStack.removeLast())
                    }
                    opStack.add(token)
                }
                else -> {

                }
            }
        }
        while (opStack.isNotEmpty()) {
            postfix.add(opStack.removeLast())
        }
    }

    fun evaluatePostfix(): String {
        for (token in postfix) {
            when {
                token.toDoubleOrNull() != null -> stack.add(token.toDouble())
                token in operators -> {
                    if (stack.size < 2 && token != "r" && token != "e") {
                        return "Error: Operadores binarios requieren dos operandos."
                    }
                    val b = stack.removeLast()
                    val a = if (stack.isNotEmpty()) stack.removeLast() else 0.0

                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        "^" -> Math.pow(a, b)
                        "r" -> Math.sqrt(b)
                        "e" -> Math.exp(b)
                        else -> {
                            return "Operador no soportado."
                        }
                    }
                    stack.add(result)
                }
                else -> {
                    return "Token no reconocido: $token"
                }
            }
        }
        return if (stack.size != 1) {
            "Error: La expresion no está correctamente balanceada."
        } else {
            stack.last().toString()
        }
    }

    toPostfix()
    return evaluatePostfix()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CalculatorUI()
}
