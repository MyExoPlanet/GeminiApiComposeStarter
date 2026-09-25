package com.fahim.geminiApiComposeStarter.data

import kotlinx.coroutines.flow.Flow

class ChatHistoryRepository(
    private val dao: ChatMessageDao,
) : ChatHistoryDataSource {

    override fun getMessages(): Flow<List<ChatMessageEntity>> {
        return dao.getAllMessages()
    }

    override suspend fun saveMessage(message: ChatMessage) {
        dao.insertMessage(
            ChatMessageEntity(
                id = message.id,
                text = message.text,
                sender = message.sender,
            )
        )
    }

    override suspend fun clearHistory() {
        dao.deleteAllMessages()
    }
}