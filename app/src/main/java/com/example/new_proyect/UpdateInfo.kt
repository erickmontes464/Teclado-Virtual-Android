package com.example.new_proyect

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la información de actualización desde GitHub
 */
data class UpdateInfo(
    @SerializedName("latest_version_code")
    val latestVersionCode: Int,
    
    @SerializedName("latest_version_name")
    val latestVersionName: String,
    
    @SerializedName("download_url")
    val downloadUrl: String,
    
    @SerializedName("changelog")
    val changelog: String
)

