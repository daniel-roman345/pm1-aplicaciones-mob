package com.ecommerce.ecommerceapp.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // ------------------------------------------------------------------
    //  A DONDE APUNTA LA APP
    // ------------------------------------------------------------------
    // Descomenta UNA sola linea segun donde estes corriendo la app.
    //
    // 1) EMULADOR de Android Studio. Para el emulador, la maquina donde
    //    corre Flask NO es 127.0.0.1 (eso seria el emulador hablando
    //    consigo mismo): es la direccion especial 10.0.2.2, que el
    //    emulador redirige al PC anfitrion.
    private const val BASE_URL = "http://10.0.2.2:5050/"

    // 2) CELULAR FISICO por USB o WiFi. Aqui si va la IP del PC en la red.
    //    Ambos tienen que estar en la misma WiFi. Si tu IP cambia, mirala
    //    en la terminal donde arranca Flask: la imprime como
    //    "Running on http://<tu-ip>:5050".
    //private const val BASE_URL = "http://192.168.20.8:5050/"

    // 3) SERVIDOR DESPLEGADO en Render. No sirve para el plan de
    //    mejoramiento, porque entonces la app no toca tu backend local y
    //    la terminal no registra ninguna peticion.
    //private const val BASE_URL = "https://ecommerce-api-python.onrender.com/"



    private val debugInterceptor = Interceptor { chain ->
        val request = chain.request()
        Log.d("API_DEBUG", "URL: ${request.url}")
        Log.d("API_DEBUG", "Method: ${request.method}")
        Log.d("API_DEBUG", "Headers: ${request.headers}")

        val response = chain.proceed(request)

        Log.d("API_DEBUG", "Response Code: ${response.code}")
        Log.d("API_DEBUG", "Response Message: ${response.message}")

        response
    }

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HTTP_LOG", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(debugInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}