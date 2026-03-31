package com.sobhaty.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "subha_prefs")

class SubhaRepository(private val context: Context) {
    fun getInt(key: String, default: Int): Flow<Int> = 
        context.dataStore.data.map { it[intPreferencesKey(key)] ?: default }
    
    suspend fun saveInt(key: String, value: Int) {
        context.dataStore.edit { it[intPreferencesKey(key)] = value }
    }
}
