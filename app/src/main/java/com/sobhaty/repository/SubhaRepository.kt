package com.sobhaty.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "subha_prefs")

class SubhaRepository(private val context: Context) {
    
    companion object {
        const val KEY_SELECTED_INDEX = "selected_index"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_TOOLTIP_SHOWN = "tooltip_shown"
        const val KEY_BOTTOM_BAR_PULSE_SHOWN = "bottom_bar_pulse_shown"
        const val KEY_DARK_MODE = "is_dark_mode" // مفتاح جديد
    }

    fun getInt(key: String, default: Int): Flow<Int> = 
        context.dataStore.data.map { it[intPreferencesKey(key)] ?: default }
    
    suspend fun saveInt(key: String, value: Int) {
        context.dataStore.edit { it[intPreferencesKey(key)] = value }
    }

    fun getBoolean(key: String, default: Boolean): Flow<Boolean> = 
        context.dataStore.data.map { it[booleanPreferencesKey(key)] ?: default }

    suspend fun saveBoolean(key: String, value: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey(key)] = value }
    }
}
