package com.jetpack.barcodescanner

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppDataStore(private val context: Context) {
    val APP_SELECTED_TOOLING = stringPreferencesKey("imn_selected_tooling")
    val APP_SELECTED_MESIN = stringPreferencesKey("imn_selected_mesin")
    val APP_SELECTED_OPERATOR = stringPreferencesKey("imn_selected_operator")

    val getSelectedTooling: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_SELECTED_TOOLING] ?: ""
    }

    val getSelectedMesin: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_SELECTED_MESIN] ?: ""
    }

    val getSelectedOperator: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_SELECTED_OPERATOR] ?: "NONE"
    }

    suspend fun saveSelectedTooling(tooling: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_SELECTED_TOOLING] = tooling
        }
    }

    suspend fun saveSelectedMesin(mesin: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_SELECTED_MESIN] = mesin
        }
    }

    suspend fun saveSelectedOperator(mesin: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_SELECTED_OPERATOR] = mesin
        }
    }

    suspend fun resetAllState() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}