package com.fahim.geminiApiComposeStarter.ui.chat

data class ChatUiState(
    val prompt: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val promptError: PromptError? = null,
    val errorMessage: String? = null,
)

data class ChatMessage(
    val id: Long,
    val text: String,
    val sender: Sender,
)

enum class Sender {
    USER,
    GEMINI,
}

enum class PromptError {
    EMPTY
}