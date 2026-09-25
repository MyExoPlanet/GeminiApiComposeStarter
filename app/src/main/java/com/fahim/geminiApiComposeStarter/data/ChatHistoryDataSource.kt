package com.fahim.geminiApiComposeStarter.data

import kotlinx.coroutines.flow.Flow

interface ChatHistoryDataSource {

    fun getMessages(): Flow<List<ChatMessageEntity>>

    suspend fun saveMessage(message: ChatMessage)

    suspend fun clearHistory()
}