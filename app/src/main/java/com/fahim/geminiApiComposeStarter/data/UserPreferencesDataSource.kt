package com.fahim.geminiApiComposeStarter.data

import kotlinx.coroutines.flow.Flow

interface UserPreferencesDataSource {

    val showTimestamps: Flow<Boolean>

    suspend fun setShowTimestamps(enabled: Boolean)
}