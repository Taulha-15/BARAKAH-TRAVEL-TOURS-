package com.barakahtravel.staff
/*
 * NetworkModule.kt – builds the shared Retrofit/OkHttp client.
 * Adds the Bearer token to every request and logs each call (Logcat tag "OkHttp").
 * Base address comes from BuildConfig.API_BASE_URL (set in app/build.gradle.kts).
 */

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Singleton that creates the API client once and reuses it. */
object NetworkModule {

    private lateinit var tokenProvider: () -> String?

    fun init(getToken: () -> String?) { tokenProvider = getToken }

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // Give up after 20 seconds so the UI shows an error instead of waiting forever
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            // Interceptor 1: attach the saved token to every request
            .addInterceptor { chain ->
                val token = runCatching { tokenProvider() }.getOrNull()
                val req = chain.request().newBuilder()
                    .apply { if (!token.isNullOrBlank()) header("Authorization", "Bearer $token") }
                    .build()
                chain.proceed(req)
            }
            // Interceptor 2: log method, URL and status code (BASIC keeps tokens out of the log)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}