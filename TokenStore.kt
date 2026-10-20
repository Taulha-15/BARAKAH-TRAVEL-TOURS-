package com.barakahtravel.staff

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.tokenStore by preferencesDataStore("token_store")

class TokenStore(private val context: Context) {
    companion object { private val KEY = stringPreferencesKey("jwt") }

    // Cached so the OkHttp interceptor (which is sync) can read it fast
    @Volatile private var cached: String? = null
    val token: String? get() = cached

    fun warmUp() {
        cached = runBlocking { context.tokenStore.data.map { it[KEY] }.first() }
    }

    suspend fun save(jwt: String) {
        cached = jwt
        context.tokenStore.edit { it[KEY] = jwt }
    }

    suspend fun clear() {
        cached = null
        context.tokenStore.edit { it.remove(KEY) }
    }
}