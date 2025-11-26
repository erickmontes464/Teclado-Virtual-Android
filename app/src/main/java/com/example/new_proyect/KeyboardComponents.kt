package com.example.new_proyect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GboardVisualKeyboard(
    onKeyPress: (String) -> Unit,
    isUpperCase: Boolean = false,
    isCapsLock: Boolean = false,
    isDarkMode: Boolean = false,
    keyboardHeight: androidx.compose.ui.unit.Dp = 320.dp,
    onBackspaceLongPress: () -> Unit = {}
) {
    // Calcular tamaño de teclas basado en la altura del teclado - optimizado con remember
    val baseHeight = remember { 320.dp }
    val baseKeyHeight = remember { 48.dp }
    val scaleFactor = remember(keyboardHeight) { keyboardHeight.value / baseHeight.value }
    val keyHeight = remember(scaleFactor) { (baseKeyHeight.value * scaleFactor).dp }
    val fontSize = remember(scaleFactor) { (18f * scaleFactor).sp }
    val verticalPadding = remember(scaleFactor) { (12f * scaleFactor).dp }
    val rowSpacing = remember(scaleFactor) { (8f * scaleFactor).dp }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF))
            .padding(top = verticalPadding, bottom = 0.dp, start = 3.dp, end = 3.dp),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        // Primera fila: Q W E R T Y U I O P
        KeyboardRow(
            keys = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            onKeyPress = onKeyPress,
            isUpperCase = isUpperCase || isCapsLock,
            isDarkMode = isDarkMode,
            keyHeight = keyHeight,
            fontSize = fontSize
        )
        
        // Segunda fila: A S D F G H J K L
        KeyboardRow(
            keys = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            startSpacer = true,
            onKeyPress = onKeyPress,
            isUpperCase = isUpperCase || isCapsLock,
            isDarkMode = isDarkMode,
            keyHeight = keyHeight,
            fontSize = fontSize
        )
        
        // Tercera fila: Shift Z X C V B N M Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Shift key (más grande)
            KeyboardKey(
                text = "⇧",
                modifier = Modifier.weight(1.5f),
                onKeyPress = onKeyPress,
                isPressed = isUpperCase || isCapsLock,
                isCapsLock = isCapsLock,
                isSpecialKey = true,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "Z",
                modifier = Modifier.weight(1f),
                onKeyPress = onKeyPress,
                isUpperCase = isUpperCase || isCapsLock,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "X",
                modifier = Modifier.weight(1f),
                onKeyPress = onKeyPress,
                isUpperCase = isUpperCase || isCapsLock,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "C",
                modifier = Modifier.weight(1f),
                onKeyPress = onKeyPress,
                isUpperCase = isUpperCase || isCapsLock,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "V",
                modifier = Modifier.weight(1f),
                onKeyPress = onKeyPress,
                isUpperCase = isUpperCase || isCapsLock,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "B",
                modifier = Modifier.weight(1f),
                onKeyPress = onKeyPress,
                isUpperCase = isUpperCase || isCapsLock,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "N",
                modifier = Modifier.weight(1f),
                onKeyPress = onKeyPress,
                isUpperCase = isUpperCase || isCapsLock,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "M",
                modifier = Modifier.weight(1f),
                onKeyPress = onKeyPress,
                isUpperCase = isUpperCase || isCapsLock,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            // Backspace key (más grande)
            KeyboardKey(
                text = "⌫",
                modifier = Modifier.weight(1.5f),
                onKeyPress = onKeyPress,
                onLongPress = onBackspaceLongPress,
                isSpecialKey = true,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
        }
        
        // Cuarta fila: 123 . Espacio Return
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyboardKey(
                text = "123",
                modifier = Modifier.weight(1.5f),
                onKeyPress = onKeyPress,
                isSpecialKey = true,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = ".",
                modifier = Modifier.weight(1.5f),
                onKeyPress = onKeyPress,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "espacio",
                modifier = Modifier.weight(4f),
                onKeyPress = onKeyPress,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "⏎",
                modifier = Modifier.weight(1.5f),
                onKeyPress = onKeyPress,
                isSpecialKey = true,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
        }
    }
}

@Composable
fun KeyboardRow(
    keys: List<String>,
    startSpacer: Boolean = false,
    onKeyPress: (String) -> Unit,
    isUpperCase: Boolean = false,
    isDarkMode: Boolean = false,
    keyHeight: androidx.compose.ui.unit.Dp = 48.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (startSpacer) {
            Spacer(modifier = Modifier.weight(0.25f))
        }
        keys.forEach { key ->
            KeyboardKey(
                text = key,
                modifier = Modifier.weight(1f),
                onKeyPress = onKeyPress,
                isUpperCase = isUpperCase,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
        }
        if (startSpacer) {
            Spacer(modifier = Modifier.weight(0.25f))
        }
    }
}

@Composable
fun KeyboardKey(
    text: String,
    modifier: Modifier = Modifier,
    onKeyPress: (String) -> Unit = {},
    onLongPress: () -> Unit = {},
    isUpperCase: Boolean = false,
    isPressed: Boolean = false,
    isCapsLock: Boolean = false,
    isSpecialKey: Boolean = false,
    isDarkMode: Boolean = false,
    keyHeight: androidx.compose.ui.unit.Dp = 48.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    // Optimizar: calcular displayText una vez
    val displayText = remember(text, isUpperCase) {
        when (text) {
            "espacio" -> "espacio"
            "⌫" -> "⌫"
            "⇧" -> "⇧"
            "⏎" -> "⏎"
            "return" -> "⏎"
            "123" -> "123"
            "ABC" -> "ABC"
            "#+=" -> "#+="
            else -> if (isUpperCase) text else text.lowercase()
        }
    }
    
    // Interacción para detectar cuando se presiona
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    val isActuallyPressed = isPressed || isPressedState
    val coroutineScope = rememberCoroutineScope()
    
    // Detectar presión larga para backspace
    LaunchedEffect(isPressedState) {
        if (isPressedState && text == "⌫") {
            delay(500L) // Esperar 500ms antes de activar eliminación rápida
            if (isPressedState) {
                // Mantener presionado: eliminar palabras repetidamente
                while (isPressedState) {
                    onLongPress()
                    delay(50L) // Intervalo entre eliminaciones
                }
            }
        }
    }
    
    // Optimizar: calcular colores con remember según el tema
    val backgroundColor = remember(isSpecialKey, isActuallyPressed, isDarkMode) {
        if (isDarkMode) {
            if (isSpecialKey) {
                if (isActuallyPressed) Color(0xFF4A4A4A) else Color(0xFF3A3A3A)
            } else {
                if (isActuallyPressed) Color(0xFF3A3A3A) else Color(0xFF2A2A2A)
            }
        } else {
            if (isSpecialKey) {
                if (isActuallyPressed) Color(0xFF9E9E9E) else Color(0xFFE0E0E0)
            } else {
                if (isActuallyPressed) Color(0xFFD8D8D8) else Color(0xFFFFFFFF)
            }
        }
    }
    
    val borderColor = remember(isActuallyPressed, isDarkMode) {
        if (isDarkMode) {
            if (isActuallyPressed) Color(0xFF4A4A4A) else Color(0xFF3A3A3A)
        } else {
            if (isActuallyPressed) Color(0xFFB0B0B0) else Color(0xFFE0E0E0)
        }
    }
    
    val textColor = remember(isSpecialKey, isActuallyPressed, isDarkMode) {
        if (isDarkMode) {
            if (isSpecialKey) {
                Color(0xFFFFFFFF)
            } else {
                Color(0xFFE0E0E0)
            }
        } else {
            if (isSpecialKey) {
                if (isActuallyPressed) Color(0xFFE0E0E0) else Color(0xFFFFFFFF)
            } else {
                if (isActuallyPressed) Color(0xFF000000) else Color(0xFF1A1A1A)
            }
        }
    }
    
    Box(
        modifier = modifier
            .height(keyHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                1.5.dp,
                borderColor,
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onKeyPress(text) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun NumericKeyboard(
    onKeyPress: (String) -> Unit,
    isSymbolMode: Boolean = false,
    isDarkMode: Boolean = false,
    keyboardHeight: androidx.compose.ui.unit.Dp = 320.dp
) {
    // Calcular tamaño de teclas basado en la altura del teclado - optimizado con remember
    val baseHeight = remember { 320.dp }
    val baseKeyHeight = remember { 48.dp }
    val scaleFactor = remember(keyboardHeight) { keyboardHeight.value / baseHeight.value }
    val keyHeight = remember(scaleFactor) { (baseKeyHeight.value * scaleFactor).dp }
    val fontSize = remember(scaleFactor) { (18f * scaleFactor).sp }
    val verticalPadding = remember(scaleFactor) { (12f * scaleFactor).dp }
    val rowSpacing = remember(scaleFactor) { (8f * scaleFactor).dp }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF))
            .padding(top = verticalPadding, bottom = 0.dp, start = 3.dp, end = 3.dp),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        if (isSymbolMode) {
            // Modo símbolos - Primera fila: [ ] { } # % ^ * + =
            KeyboardRow(
                keys = listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
                onKeyPress = onKeyPress,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            
            // Segunda fila: _ \ | ~ < > € £ ¥ •
            KeyboardRow(
                keys = listOf("_", "\\", "|", "~", "<", ">", "€", "£", "¥", "•"),
                onKeyPress = onKeyPress,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            
            // Tercera fila: . , ? ! ' Backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                KeyboardKey(text = ".", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = ",", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = "?", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = "!", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = "'", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = "\"", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = ":", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = ";", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = "⌫", modifier = Modifier.weight(1.5f), onKeyPress = onKeyPress, isSpecialKey = true, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
            }
        } else {
            // Modo números - Primera fila: 1 2 3 4 5 6 7 8 9 0
            KeyboardRow(
                keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                onKeyPress = onKeyPress,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            
            // Segunda fila: - / : ; ( ) $ & @ "
            KeyboardRow(
                keys = listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\""),
                onKeyPress = onKeyPress,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            
            // Tercera fila: #+= . , ? ! ' Backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                KeyboardKey(
                    text = "#+=",
                    modifier = Modifier.weight(1.5f),
                    onKeyPress = onKeyPress,
                    isSpecialKey = true,
                    isDarkMode = isDarkMode,
                    keyHeight = keyHeight,
                    fontSize = fontSize
                )
                KeyboardKey(text = ".", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = ",", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = "?", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = "!", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(text = "'", modifier = Modifier.weight(1f), onKeyPress = onKeyPress, isDarkMode = isDarkMode, keyHeight = keyHeight, fontSize = fontSize)
                KeyboardKey(
                    text = "⌫",
                    modifier = Modifier.weight(1.5f),
                    onKeyPress = onKeyPress,
                    isSpecialKey = true,
                    isDarkMode = isDarkMode,
                    keyHeight = keyHeight,
                    fontSize = fontSize
                )
            }
        }
        
            // Cuarta fila: ABC Espacio Return (sin punto, espacio ampliado)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyboardKey(
                text = "ABC",
                modifier = Modifier.weight(1.5f),
                onKeyPress = onKeyPress,
                isSpecialKey = true,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "espacio",
                modifier = Modifier.weight(5.5f),
                onKeyPress = onKeyPress,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
            KeyboardKey(
                text = "⏎",
                modifier = Modifier.weight(1.5f),
                onKeyPress = onKeyPress,
                isSpecialKey = true,
                isDarkMode = isDarkMode,
                keyHeight = keyHeight,
                fontSize = fontSize
            )
        }
    }
}

