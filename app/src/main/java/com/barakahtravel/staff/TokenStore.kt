package com.barakahtravel.staff
/*
 * TokenStore.kt – stores the login token on the device using Jetpack DataStore.
 * A cached copy lets the (synchronous) network interceptor read the token quickly.
 */

import android.util.Log
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.tokenStore by preferencesDataStore("token_store")

/** Saves, reads and clears the JWT. The token itself is never written to the log. */
class TokenStore(private val context: Context) {
    companion object { private val KEY = stringPreferencesKey("jwt") }

    // Cached so the OkHttp interceptor (which is sync) can read it fast
    @Volatile private var cached: String? = null
    val token: String? get() = cached

    fun warmUp() {
        cached = runBlocking { context.tokenStore.data.map { it[KEY] }.first() }
    }

    suspend fun save(jwt: String) {
        Log.d("BarakahStaff", "TokenStore: token saved")
        cached = jwt
        context.tokenStore.edit { it[KEY] = jwt }
    }

    suspend fun clear() {
        Log.d("BarakahStaff", "TokenStore: token cleared (signed out)")
        cached = null
        context.tokenStore.edit { it.remove(KEY) }
    }
}