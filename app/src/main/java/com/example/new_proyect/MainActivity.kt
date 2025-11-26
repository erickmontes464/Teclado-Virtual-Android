package com.example.new_proyect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.new_proyect.ui.theme.New_ProyectTheme

class MainActivity : ComponentActivity() {
    private lateinit var updateManager: UpdateManager
    private var onPermissionGranted: (() -> Unit)? = null
    
    // Launcher para permisos de almacenamiento
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, continuar con la actualización
            onPermissionGranted?.invoke()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        updateManager = UpdateManager(this)
        
        // Optimizar: configurar el contenido de forma eficiente
        setContent {
            New_ProyectTheme {
                SettingsScreen(
                    updateManager = updateManager,
                    requestPermission = { onGranted ->
                        onPermissionGranted = onGranted
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            onGranted()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    updateManager: UpdateManager,
    requestPermission: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "ALMACEN CENTRAL",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "TECLADO",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "ver.1",
                color = Color(0xFF888888),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "© 2025 NOVIEMBRE EV. Todos los derechos reservados.",
                color = Color(0xFF666666),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Activa el teclado en Ajustes",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            // Botón para abrir ajustes
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF3A3A3A))
                    .border(1.dp, Color(0xFF4A4A4A), RoundedCornerShape(12.dp))
                    .clickable {
                        try {
                            // Intentar abrir directamente la configuración de métodos de entrada
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Si falla, abrir ajustes generales
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Abrir Ajustes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "1. Activa 'ABA_KEYBOARD'\n2. Selecciónalo como predeterminado",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Campo de texto de prueba
            Text(
                text = "Campo de prueba:",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            
            var testText by remember { mutableStateOf("") }
            
            TextField(
                value = testText,
                onValueChange = { testText = it },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A2A2A),
                    unfocusedContainerColor = Color(0xFF2A2A2A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0xFF4A4A4A),
                    unfocusedIndicatorColor = Color(0xFF3A3A3A),
                    disabledIndicatorColor = Color.Transparent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                placeholder = {
                    Text(
                        text = "Toca aquí para probar el teclado",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón para buscar actualización
            var showVersionDialog by remember { mutableStateOf(false) }
            var showUpdateDialog by remember { mutableStateOf(false) }
            var appVersion by remember { mutableStateOf("") }
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF3A3A3A))
                    .border(1.dp, Color(0xFF4A4A4A), RoundedCornerShape(12.dp))
                    .clickable {
                        // Función para iniciar la actualización
                        fun startUpdate() {
                            // Verificar permiso de instalación
                            if (!updateManager.canInstallPackages()) {
                                updateManager.requestInstallPermission()
                                return
                            }
                            
                            // Iniciar proceso de actualización
                            isLoading = true
                            errorMessage = null
                            
                            // Obtener información de actualización en una coroutine
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                try {
                                    val service = UpdateServiceFactory.create()
                                    val info = service.getUpdateInfo()
                                    updateInfo = info
                                    
                                    // Descargar e instalar
                                    val apkFile = updateManager.downloadApk(info.downloadUrl)
                                    if (apkFile != null) {
                                        updateManager.installApk(apkFile)
                                    } else {
                                        errorMessage = "Error al descargar la actualización"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                    e.printStackTrace()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                        
                        // Verificar permisos de almacenamiento (solo para Android < 13)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            val hasStoragePermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (!hasStoragePermission) {
                                requestPermission { startUpdate() }
                                return@clickable
                            }
                        }
                        
                        startUpdate()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLoading) "Descargando..." else "Buscar actualización",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Diálogo para mostrar errores
            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { errorMessage = null },
                    title = {
                        Text(
                            text = "Error",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            color = Color.White
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { errorMessage = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3A3A3A)
                            )
                        ) {
                            Text("Aceptar", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFF2A2A2A)
                )
            }
        }
    }
}