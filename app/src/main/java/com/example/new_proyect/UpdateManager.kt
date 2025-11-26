package com.example.new_proyect

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Gestor de actualizaciones que maneja la descarga e instalación de APKs desde GitHub
 */
class UpdateManager(private val context: Context) {
    
    /**
     * Descarga el APK desde la URL proporcionada
     */
    suspend fun downloadApk(downloadUrl: String): File? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(downloadUrl)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext null
            }
            
            // Crear directorio para el APK si no existe
            val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            
            // Crear archivo APK
            val apkFile = File(downloadDir, "keyboard_update.apk")
            
            // Escribir el contenido descargado al archivo
            response.body?.byteStream()?.use { input ->
                FileOutputStream(apkFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            apkFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Instala el APK descargado usando FileProvider
     */
    fun installApk(apkFile: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Usar FileProvider para Android 7.0+
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )
                } else {
                    // Para versiones anteriores
                    Uri.fromFile(apkFile)
                }
                
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Verifica si la app tiene permiso para instalar paquetes
     */
    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // Para versiones anteriores a Android 8.0, siempre se puede instalar
        }
    }
    
    /**
     * Abre la configuración para permitir la instalación de apps desde fuentes desconocidas
     */
    fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}

