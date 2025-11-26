package com.example.new_proyect

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowInsets
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.text.TextUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.new_proyect.ui.theme.New_ProyectTheme

/**
 * Servicio de teclado virtual personalizado para Almacén Central
 * Creado por EV
 */
class SimpleKeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {
    
    // Constantes
    companion object {
        private const val MAX_PARENT_ITERATIONS = 10
        private const val PARENT_PANEL_ID_NAME = "parentPanel"
        private const val PARENT_PANEL_ID_NAMESPACE = "android"
        private const val VIBRATION_DURATION_MS = 5L // Duración de vibración en milisegundos
        private const val VIBRATION_AMPLITUDE = 100 // Amplitud de vibración (0-255, aumentada para mayor intensidad)
        private const val DOUBLE_TAP_TIMEOUT_MS = 500L // Tiempo máximo entre dos toques para considerar doble toque
        private const val LONG_PRESS_DELAY_MS = 500L // Tiempo para considerar presión larga
        private const val BACKSPACE_REPEAT_DELAY_MS = 50L // Intervalo entre eliminaciones cuando se mantiene presionado
    }
    
    // Estados del teclado
    private var inputConnection: InputConnection? = null
    private val lifecycleOwner = SimpleLifecycleOwner()
    private val savedStateController = SavedStateRegistryController.create(this)
    private var parentPanelId: Int = 0
    private var lastShiftPressTime: Long = 0L // Para detectar doble toque en Shift
    private var wasLastKeyShift: Boolean = false // Para detectar si la última tecla fue Shift
    
    // Vibrator para feedback háptico - inicializado una vez
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Implementación de LifecycleOwner
    override val lifecycle: Lifecycle
        get() = lifecycleOwner.lifecycle
    
    // Implementación de SavedStateRegistryOwner
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry
    
    init {
        savedStateController.performRestore(null)
    }

    override fun onCreateInputView(): View {
        val composeView = createComposeView()
        val container = createContainerView(composeView)
        
        container.addView(composeView)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        
        return container
    }
    
    /**
     * Crea y configura el ComposeView con los owners necesarios
     */
    private fun createComposeView(): ComposeView {
        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(this@SimpleKeyboardService)
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner)
            )
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            
            setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    New_ProyectTheme {
                        KeyboardContent()
                    }
                }
            }
        }
    }
    
    /**
     * Crea un contenedor ViewGroup que propaga los owners en la jerarquía
     * y consume las Window Insets de navegación
     */
    private fun createContainerView(childView: View): ViewGroup {
        return object : ViewGroup(this) {
            init {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(this@SimpleKeyboardService)
                
                // Configurar para consumir las insets de navegación
                setOnApplyWindowInsetsListener { view, insets ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Consumir las insets de navegación para que el teclado llegue hasta abajo
                        WindowInsets.CONSUMED
                    } else {
                        @Suppress("DEPRECATION")
                        // Para versiones anteriores, consumir las insets
                        view.onApplyWindowInsets(insets.consumeSystemWindowInsets())
                    }
                }
            }
            
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()
                propagateViewTreeOwners()
            }
            
            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
                getChildAt(0)?.layout(0, 0, r - l, b - t)
            }
            
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                getChildAt(0)?.let { child ->
                    child.measure(widthMeasureSpec, heightMeasureSpec)
                    setMeasuredDimension(child.measuredWidth, child.measuredHeight)
                } ?: super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }
    
    /**
     * Propaga los ViewTreeOwners en toda la jerarquía de vistas
     */
    private fun ViewGroup.propagateViewTreeOwners() {
        var parent: ViewParent? = this.parent
        var iterations = 0
        
        while (parent != null && iterations < MAX_PARENT_ITERATIONS) {
            if (parent is ViewGroup) {
                parent.setViewTreeLifecycleOwner(lifecycleOwner)
                parent.setViewTreeSavedStateRegistryOwner(this@SimpleKeyboardService)
                if (parentPanelId != 0 && parent.id == parentPanelId) {
                    break
                }
            }
            parent = parent.parent
            iterations++
        }
        
        // Establecer en todos los hijos - optimizado
        repeat(childCount) { i ->
            getChildAt(i)?.apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(this@SimpleKeyboardService)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        cacheParentPanelId()
    }
    
    /**
     * Cachea el ID del parentPanel para optimización
     */
    private fun cacheParentPanelId() {
        parentPanelId = try {
            resources.getIdentifier(PARENT_PANEL_ID_NAME, "id", PARENT_PANEL_ID_NAMESPACE)
        } catch (e: Exception) {
            0
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        inputConnection = currentInputConnection
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        inputConnection = null
    }

    override fun onEvaluateInputViewShown(): Boolean = true

    override fun onEvaluateFullscreenMode(): Boolean = false

    /**
     * Vibra el dispositivo cuando se presiona una tecla - optimizado
     */
    private fun vibrate() {
        val v = vibrator ?: return
        try {
            if (!v.hasVibrator()) return
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION_MS, VIBRATION_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(VIBRATION_DURATION_MS)
            }
        } catch (e: Exception) {
            // Ignorar errores de vibración silenciosamente
        }
    }
    
    /**
     * Elimina una palabra completa desde el cursor
     */
    private fun deleteWord(connection: InputConnection) {
        val textBeforeCursor = connection.getTextBeforeCursor(100, 0) ?: return
        if (textBeforeCursor.isEmpty()) {
            connection.deleteSurroundingText(1, 0)
            return
        }
        
        // Encontrar el inicio de la palabra actual o el espacio anterior
        var deleteLength = 0
        var foundNonSpace = false
        
        for (i in textBeforeCursor.length - 1 downTo 0) {
            val char = textBeforeCursor[i]
            if (char.isWhitespace()) {
                if (foundNonSpace) {
                    // Encontramos el inicio de la palabra
                    break
                } else {
                    // Seguir eliminando espacios
                    deleteLength++
                }
            } else {
                foundNonSpace = true
                deleteLength++
            }
        }
        
        if (deleteLength > 0) {
            connection.deleteSurroundingText(deleteLength, 0)
        } else {
            connection.deleteSurroundingText(1, 0)
        }
    }
    
    /**
     * Maneja las pulsaciones de teclas y actualiza el estado
     */
    private fun handleKeyPress(
        key: String,
        currentUpperCase: Boolean,
        currentNumericMode: Boolean,
        currentSymbolMode: Boolean,
        currentCapsLock: Boolean,
        onStateChange: (Boolean, Boolean, Boolean, Boolean) -> Unit
    ) {
        // Vibrar al presionar cualquier tecla
        vibrate()
        
        val connection = inputConnection ?: currentInputConnection ?: return

        when (key) {
            KeyConstants.SHIFT -> {
                val currentTime = System.currentTimeMillis()
                val timeSinceLastPress = currentTime - lastShiftPressTime
                
                if (wasLastKeyShift && timeSinceLastPress < DOUBLE_TAP_TIMEOUT_MS && !currentCapsLock) {
                    // Doble toque: activar caps lock
                    onStateChange(true, currentNumericMode, currentSymbolMode, true)
                } else if (currentCapsLock) {
                    // Si está en caps lock, desactivarlo
                    onStateChange(false, currentNumericMode, currentSymbolMode, false)
                } else {
                    // Toque simple: alternar mayúsculas
                    onStateChange(!currentUpperCase, currentNumericMode, currentSymbolMode, false)
                }
                
                lastShiftPressTime = currentTime
                wasLastKeyShift = true
            }
            KeyConstants.BACKSPACE -> {
                wasLastKeyShift = false
                // Eliminar un carácter (la presión larga se maneja en el composable)
                connection.deleteSurroundingText(1, 0)
            }
            KeyConstants.SPACE -> {
                connection.commitText(" ", 1)
            }
            KeyConstants.ENTER, KeyConstants.RETURN -> {
                // Enviar acción de editor (buscar, enviar, etc.) o Enter según el contexto
                val editorInfo = currentInputEditorInfo
                if (editorInfo != null) {
                    val action = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
                    if (action != EditorInfo.IME_ACTION_NONE) {
                        // Si hay una acción específica (buscar, enviar, etc.), ejecutarla
                        connection.performEditorAction(action)
                    } else {
                        // Si no hay acción específica, enviar Enter como evento de tecla
                        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                        connection.sendKeyEvent(event)
                        val eventUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER)
                        connection.sendKeyEvent(eventUp)
                    }
                } else {
                    // Si no hay editorInfo, enviar Enter como evento de tecla
                    val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                    connection.sendKeyEvent(event)
                    val eventUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER)
                    connection.sendKeyEvent(eventUp)
                }
            }
            KeyConstants.NUMERIC_MODE -> {
                onStateChange(currentUpperCase, true, false, currentCapsLock)
            }
            KeyConstants.ALPHABETIC_MODE -> {
                onStateChange(currentUpperCase, false, false, currentCapsLock)
            }
            KeyConstants.SYMBOL_MODE -> {
                onStateChange(currentUpperCase, currentNumericMode, true, currentCapsLock)
            }
            else -> {
                wasLastKeyShift = false
                val charToAdd = when {
                    currentNumericMode || currentSymbolMode -> key
                    currentUpperCase || currentCapsLock -> key
                    else -> key.lowercase()
                }
                connection.commitText(charToAdd, 1)
                
                // Auto-desactivar mayúsculas después de escribir una letra solo si NO está en caps lock
                if (!currentNumericMode && !currentSymbolMode && !currentCapsLock && currentUpperCase) {
                    onStateChange(false, currentNumericMode, currentSymbolMode, false)
                }
            }
        }
    }
    
    /**
     * Contenido principal del teclado con estado
     */
    @Composable
    private fun KeyboardContent() {
        var isUpperCase by remember { mutableStateOf(false) }
        var isNumericMode by remember { mutableStateOf(false) }
        var isSymbolMode by remember { mutableStateOf(false) }
        var isCapsLock by remember { mutableStateOf(false) }
        var isDarkMode by remember { mutableStateOf(false) }
        
        KeyboardView(
            onKeyPress = { key ->
                handleKeyPress(key, isUpperCase, isNumericMode, isSymbolMode, isCapsLock) { upper, numeric, symbol, capsLock ->
                    isUpperCase = upper
                    isNumericMode = numeric
                    isSymbolMode = symbol
                    isCapsLock = capsLock
                }
            },
            onBackspaceLongPress = { 
                val conn = inputConnection ?: currentInputConnection
                conn?.let { deleteWord(it) }
            },
            isUpperCase = isUpperCase || isCapsLock,
            isNumericMode = isNumericMode,
            isSymbolMode = isSymbolMode,
            isCapsLock = isCapsLock,
            onUpperCaseChange = { 
                isUpperCase = it
                if (!it) isCapsLock = false // Desactivar caps lock si se desactiva manualmente
            },
            onNumericModeChange = { isNumericMode = it },
            onSymbolModeChange = { isSymbolMode = it },
            isDarkMode = isDarkMode,
            onDarkModeChange = { isDarkMode = it }
        )
    }
}

/**
 * Constantes para las teclas especiales
 */
private object KeyConstants {
    const val SHIFT = "⇧"
    const val BACKSPACE = "⌫"
    const val SPACE = "espacio"
    const val ENTER = "⏎"
    const val RETURN = "return"
    const val NUMERIC_MODE = "123"
    const val ALPHABETIC_MODE = "ABC"
    const val SYMBOL_MODE = "#+="
}

/**
 * Constantes de diseño del teclado
 */
private object KeyboardConstants {
    val INITIAL_HEIGHT = 320.dp
    val MINIMUM_HEIGHT = 320.dp
    val HIDDEN_KEYBOARD_HEIGHT = 88.dp
    val TOP_BAR_HEIGHT = 40.dp
    val BUTTON_SIZE = 55.dp
    val BUTTON_HEIGHT = 32.dp
    val BOTTOM_PADDING_VISIBLE = 0.dp // Sin espacio inferior, teclado en posición 0
    val BOTTOM_PADDING_HIDDEN = 48.dp
    val TOP_PADDING = 8.dp
    val MAX_HEIGHT_SCREEN_RATIO = 0.5f
    
    // Colores modo claro
    val KEY_BACKGROUND_DEFAULT_LIGHT = Color(0xFFF5F5F5)
    val KEY_BACKGROUND_PRESSED_LIGHT = Color(0xFFD8D8D8)
    val KEY_BORDER_DEFAULT_LIGHT = Color(0xFFE0E0E0)
    val KEY_BORDER_PRESSED_LIGHT = Color(0xFFB0B0B0)
    val KEYBOARD_BACKGROUND_LIGHT = Color(0xFFFFFFFF)
    val KEY_TEXT_COLOR_LIGHT = Color(0xFF1A1A1A)
    val KEY_SPECIAL_BACKGROUND_LIGHT = Color(0xFFE0E0E0)
    val KEY_SPECIAL_TEXT_LIGHT = Color(0xFFFFFFFF)
    val TOP_BAR_BUTTON_DEFAULT_LIGHT = Color(0xFFE8E8E8)
    val TOP_BAR_BUTTON_ACTIVE_LIGHT = Color(0xFF2196F3)
    val TOP_BAR_BUTTON_BORDER_DEFAULT_LIGHT = Color(0xFFD0D0D0)
    val TOP_BAR_BUTTON_BORDER_ACTIVE_LIGHT = Color(0xFF1976D2)
    val TOP_BAR_TEXT_LIGHT = Color(0xFFB0B0B0)
    
    // Colores modo oscuro
    val KEY_BACKGROUND_DEFAULT_DARK = Color(0xFF2A2A2A)
    val KEY_BACKGROUND_PRESSED_DARK = Color(0xFF3A3A3A)
    val KEY_BORDER_DEFAULT_DARK = Color(0xFF3A3A3A)
    val KEY_BORDER_PRESSED_DARK = Color(0xFF4A4A4A)
    val KEYBOARD_BACKGROUND_DARK = Color(0xFF1A1A1A)
    val KEY_TEXT_COLOR_DARK = Color(0xFFE0E0E0)
    val KEY_SPECIAL_BACKGROUND_DARK = Color(0xFF3A3A3A)
    val KEY_SPECIAL_TEXT_DARK = Color(0xFFFFFFFF)
    val TOP_BAR_BUTTON_DEFAULT_DARK = Color(0xFF2A2A2A)
    val TOP_BAR_BUTTON_ACTIVE_DARK = Color(0xFF2196F3)
    val TOP_BAR_BUTTON_BORDER_DEFAULT_DARK = Color(0xFF3A3A3A)
    val TOP_BAR_BUTTON_BORDER_ACTIVE_DARK = Color(0xFF1976D2)
    val TOP_BAR_TEXT_DARK = Color(0xFF808080)
}

/**
 * Vista principal del teclado con controles de tamaño y visibilidad
 */
@Composable
fun KeyboardView(
    onKeyPress: (String) -> Unit,
    isUpperCase: Boolean,
    isNumericMode: Boolean,
    isSymbolMode: Boolean,
    isCapsLock: Boolean = false,
    isDarkMode: Boolean = false,
    onBackspaceLongPress: () -> Unit = {},
    onUpperCaseChange: (Boolean) -> Unit,
    onNumericModeChange: (Boolean) -> Unit,
    onSymbolModeChange: (Boolean) -> Unit,
    onDarkModeChange: (Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = configuration.screenHeightDp.dp
    
    val maxHeight = remember(screenHeight) { screenHeight * KeyboardConstants.MAX_HEIGHT_SCREEN_RATIO }
    var keyboardHeight by remember { mutableStateOf(KeyboardConstants.INITIAL_HEIGHT) }
    var isResizeModeActive by remember { mutableStateOf(false) }
    var areKeysVisible by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                if (areKeysVisible) keyboardHeight 
                else KeyboardConstants.HIDDEN_KEYBOARD_HEIGHT
            )
            .background(if (isDarkMode) KeyboardConstants.KEYBOARD_BACKGROUND_DARK else KeyboardConstants.KEYBOARD_BACKGROUND_LIGHT)
            .padding(
                top = KeyboardConstants.TOP_PADDING,
                bottom = if (areKeysVisible) 
                    KeyboardConstants.BOTTOM_PADDING_VISIBLE 
                else 
                    KeyboardConstants.BOTTOM_PADDING_HIDDEN,
                start = 0.dp,
                end = 0.dp
            )
    ) {
        KeyboardTopBar(
            areKeysVisible = areKeysVisible,
            isResizeModeActive = isResizeModeActive,
            isDarkMode = isDarkMode,
            onToggleKeysVisibility = {
                areKeysVisible = !areKeysVisible
                if (!areKeysVisible) {
                    isResizeModeActive = false
                }
            },
            onToggleResizeMode = { isResizeModeActive = !isResizeModeActive },
            onToggleDarkMode = { onDarkModeChange(!isDarkMode) },
            onResize = { dragAmountY: Float ->
                keyboardHeight = with(density) {
                    (keyboardHeight - dragAmountY.toDp()).coerceIn(
                        KeyboardConstants.MINIMUM_HEIGHT,
                        maxHeight
                    )
                }
            },
            density = density
        )
        
        if (areKeysVisible) {
            KeyboardContentArea(
                isNumericMode = isNumericMode,
                isSymbolMode = isSymbolMode,
                isUpperCase = isUpperCase,
                keyboardHeight = keyboardHeight,
                onKeyPress = onKeyPress,
                onBackspaceLongPress = onBackspaceLongPress,
                onUpperCaseChange = onUpperCaseChange,
                onNumericModeChange = onNumericModeChange,
                onSymbolModeChange = onSymbolModeChange
            )
        }
    }
}

/**
 * Barra superior del teclado con botones de control
 */
@Composable
private fun KeyboardTopBar(
    areKeysVisible: Boolean,
    isResizeModeActive: Boolean,
    isDarkMode: Boolean,
    onToggleKeysVisibility: () -> Unit,
    onToggleResizeMode: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onResize: (Float) -> Unit,
    density: androidx.compose.ui.unit.Density
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(KeyboardConstants.TOP_BAR_HEIGHT)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón izquierdo: ocultar/mostrar teclas
        KeyboardControlButton(
            icon = if (areKeysVisible) "⚙" else "⌨",
            isActive = !areKeysVisible,
            onClick = onToggleKeysVisibility,
            isDarkMode = isDarkMode
        )
        
        // Área de arrastre para resize con texto "PLATANITOS"
        Box(
            modifier = Modifier
                .weight(1f)
                .pointerInput(isResizeModeActive && areKeysVisible) {
                    if (isResizeModeActive && areKeysVisible) {
                        detectDragGestures { _, dragAmount ->
                            onResize(dragAmount.y)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Texto "PLATANITOS" como firma sutil
            Text(
                text = "PLATANITOS",
                color = if (isDarkMode) KeyboardConstants.TOP_BAR_TEXT_DARK else KeyboardConstants.TOP_BAR_TEXT_LIGHT,
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif, // Fuente similar a Comfortaa (redondeada)
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.2.sp
            )
            
            if (isResizeModeActive && areKeysVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color(0xFF2196F3).copy(alpha = 0.8f))
                        .padding(horizontal = 120.dp)
                        .align(Alignment.Center)
                )
            }
        }
        
        // Botones derechos: tema oscuro/claro y resize (solo visible cuando las teclas están visibles)
        if (areKeysVisible) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de tema oscuro/claro
                KeyboardControlButton(
                    icon = if (isDarkMode) "☀" else "🌙",
                    isActive = isDarkMode,
                    onClick = onToggleDarkMode,
                    isDarkMode = isDarkMode
                )
                // Botón de resize
                KeyboardControlButton(
                    icon = "⤢",
                    isActive = isResizeModeActive,
                    onClick = onToggleResizeMode,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

/**
 * Botón de control reutilizable
 */
@Composable
private fun KeyboardControlButton(
    icon: String,
    isActive: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean = false
) {
    val backgroundColor = if (isDarkMode) {
        if (isActive) KeyboardConstants.TOP_BAR_BUTTON_ACTIVE_DARK
        else KeyboardConstants.TOP_BAR_BUTTON_DEFAULT_DARK
    } else {
        if (isActive) KeyboardConstants.TOP_BAR_BUTTON_ACTIVE_LIGHT
        else KeyboardConstants.TOP_BAR_BUTTON_DEFAULT_LIGHT
    }
    
    val borderColor = if (isDarkMode) {
        if (isActive) KeyboardConstants.TOP_BAR_BUTTON_BORDER_ACTIVE_DARK
        else KeyboardConstants.TOP_BAR_BUTTON_BORDER_DEFAULT_DARK
    } else {
        if (isActive) KeyboardConstants.TOP_BAR_BUTTON_BORDER_ACTIVE_LIGHT
        else KeyboardConstants.TOP_BAR_BUTTON_BORDER_DEFAULT_LIGHT
    }
    
    Box(
        modifier = Modifier
            .width(KeyboardConstants.BUTTON_SIZE)
            .height(KeyboardConstants.BUTTON_HEIGHT)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = if (icon == "⤢") FontWeight.Bold else FontWeight.Medium
        )
    }
}


/**
 * Área de contenido del teclado (teclas)
 */
@Composable
private fun KeyboardContentArea(
    isNumericMode: Boolean,
    isSymbolMode: Boolean,
    isUpperCase: Boolean,
    isCapsLock: Boolean = false,
    isDarkMode: Boolean = false,
    keyboardHeight: androidx.compose.ui.unit.Dp,
    onKeyPress: (String) -> Unit,
    onBackspaceLongPress: () -> Unit = {},
    onUpperCaseChange: (Boolean) -> Unit,
    onNumericModeChange: (Boolean) -> Unit,
    onSymbolModeChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        if (isNumericMode) {
            NumericKeyboard(
                onKeyPress = { key ->
                    when (key) {
                        KeyConstants.ALPHABETIC_MODE -> {
                            onNumericModeChange(false)
                            onSymbolModeChange(false)
                        }
                        KeyConstants.SYMBOL_MODE -> onSymbolModeChange(true)
                        KeyConstants.NUMERIC_MODE -> onSymbolModeChange(false)
                        else -> onKeyPress(key)
                    }
                },
                isSymbolMode = isSymbolMode,
                keyboardHeight = keyboardHeight
            )
        } else {
            GboardVisualKeyboard(
                onKeyPress = { key ->
                    when (key) {
                        KeyConstants.NUMERIC_MODE -> onNumericModeChange(true)
                        else -> onKeyPress(key) // Pasar todas las teclas, incluyendo Shift, a handleKeyPress para detectar doble toque
                    }
                },
                isUpperCase = isUpperCase,
                isCapsLock = isCapsLock,
                isDarkMode = isDarkMode,
                keyboardHeight = keyboardHeight,
                onBackspaceLongPress = onBackspaceLongPress
            )
        }
    }
}
