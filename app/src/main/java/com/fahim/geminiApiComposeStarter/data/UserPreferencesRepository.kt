package com.fahim.geminiApiComposeStarter.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

private val SHOW_TIMESTAMPS = booleanPreferencesKey(
    "show_timestamps"
)

class UserPreferencesRepository(
    private val context: Context,
) : UserPreferencesDataSource {

    override val showTimestamps: Flow<Boolean> =
        context.userPreferencesDataStore.data.map { preferences ->
            preferences[SHOW_TIMESTAMPS] ?: false
        }

    override suspend fun setShowTimestamps(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[SHOW_TIMESTAMPS] = enabled
        }
    }
}