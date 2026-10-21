package com.barakahtravel.staff

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "barakah_prefs")

class LocalStorage(private val context: Context) {

    private object Keys {
        val STAFF_NAME = stringPreferencesKey("staff_name")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val LAST_PAGE = stringPreferencesKey("last_page")
    }

    val staffName: Flow<String> = context.dataStore.data.map { it[Keys.STAFF_NAME] ?: "Staff Member" }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_MODE] ?: false }
    val notifications: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS] ?: true }
    val lastPage: Flow<String> = context.dataStore.data.map { it[Keys.LAST_PAGE] ?: "Dashboard" }

    suspend fun setStaffName(v: String) = context.dataStore.edit { it[Keys.STAFF_NAME] = v }
    suspend fun setDarkMode(v: Boolean) = context.dataStore.edit { it[Keys.DARK_MODE] = v }
    suspend fun setNotifications(v: Boolean) = context.dataStore.edit { it[Keys.NOTIFICATIONS] = v }
    suspend fun setLastPage(v: String) = context.dataStore.edit { it[Keys.LAST_PAGE] = v }
}
