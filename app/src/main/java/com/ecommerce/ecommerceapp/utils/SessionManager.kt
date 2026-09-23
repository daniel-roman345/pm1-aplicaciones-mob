package com.ecommerce.ecommerceapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")  // Cambiado de access_token a token
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_IS_ADMIN = booleanPreferencesKey("is_admin")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_ROLES = stringPreferencesKey("user_roles")
    }

    suspend fun saveUserSession(
        token: String,
        userName: String,
        userEmail: String,
        isAdmin: Boolean,
        roles: List<String> = emptyList()
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
            preferences[KEY_USER_NAME] = userName
            preferences[KEY_USER_EMAIL] = userEmail
            preferences[KEY_IS_ADMIN] = isAdmin
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_USER_ROLES] = roles.joinToString(",")
        }
    }

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_TOKEN]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_EMAIL]
    }

    val isAdmin: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_ADMIN] ?: false
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    val userRoles: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_ROLES]?.split(",") ?: emptyList()
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}