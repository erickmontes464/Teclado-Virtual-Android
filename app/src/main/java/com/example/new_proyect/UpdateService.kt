package com.example.new_proyect

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Interfaz de servicio para obtener información de actualización desde GitHub
 */
interface UpdateService {
    @GET("version_info.json")
    suspend fun getUpdateInfo(): UpdateInfo
}

/**
 * Objeto para crear instancias del servicio de actualización
 */
object UpdateServiceFactory {
    private const val BASE_URL = "https://erickmontes464.github.io/Teclado-Virtual-Android/"
    
    fun create(): UpdateService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return retrofit.create(UpdateService::class.java)
    }
}

