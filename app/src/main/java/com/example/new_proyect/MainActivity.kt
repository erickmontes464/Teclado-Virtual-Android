package com.example.new_proyect

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Optimizar: configurar el contenido de forma eficiente
        setContent {
            New_ProyectTheme {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
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
        }
    }
}