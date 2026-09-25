package com.fahim.geminiApiComposeStarter.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fahim.geminiApiComposeStarter.data.ChatMessage
import com.fahim.geminiApiComposeStarter.data.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.fahim.geminiApiComposeStarter.data.ChatHistoryDataSource
import com.fahim.geminiApiComposeStarter.data.UserPreferencesDataSource

class ChatViewModel(
    private val repository: GeminiRepository,
    private val chatHistoryRepository: ChatHistoryDataSource,
    private val userPreferencesRepository: UserPreferencesDataSource,
    private val hasApiKey: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadChatHistory()
        loadUserPreferences()
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            chatHistoryRepository.getMessages().collect { messages ->
                _uiState.update { state ->
                    state.copy(
                        messages = messages.map { message ->
                            ChatMessage(
                                id = message.id,
                                text = message.text,
                                sender = message.sender,
                            )
                        }
                    )
                }
            }
        }
    }

    private fun loadUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.showTimestamps.collect { showTimestamps ->
                _uiState.update {
                    it.copy(showTimestamps = showTimestamps)
                }
            }
        }
    }

    fun onPromptChange(value: String) {
        _uiState.update {
            it.copy(
                prompt = value,
                promptError = null,
            )
        }
    }

    fun setShowTimestamps(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowTimestamps(enabled)
        }
    }

    fun onSend() {
        val prompt = _uiState.value.prompt.trim()

        if (prompt.isEmpty()) {
            _uiState.update {
                it.copy(promptError = PromptError.EMPTY)
            }
            return
        }

        if (!hasApiKey) {
            _uiState.update {
                it.copy(errorMessage = MISSING_API_KEY_MESSAGE)
            }
            return
        }

        if (_uiState.value.isLoading) return

        val userMessage = ChatMessage(
            id = System.currentTimeMillis(),
            text = prompt,
            sender = "USER",
        )

        _uiState.update {
            it.copy(
                prompt = "",
                isLoading = true,
                errorMessage = null,
                promptError = null,
            )
        }

        viewModelScope.launch {
            chatHistoryRepository.saveMessage(userMessage)

            repository.generateText(prompt).fold(
                onSuccess = { text ->
                    val geminiMessage = ChatMessage(
                        id = System.currentTimeMillis(),
                        text = text,
                        sender = "GEMINI",
                    )

                    chatHistoryRepository.saveMessage(geminiMessage)

                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                                ?: "Something went wrong",
                        )
                    }
                },
            )
        }
    }

    companion object {

        const val MISSING_API_KEY_MESSAGE =
            "GEMINI_API_KEY is missing. Add it to local.properties and rebuild."

        fun factory(
            repository: GeminiRepository,
            chatHistoryRepository: ChatHistoryDataSource,
            userPreferencesRepository: UserPreferencesDataSource,
            hasApiKey: Boolean,
        ) = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
            ): T = ChatViewModel(
                repository = repository,
                chatHistoryRepository = chatHistoryRepository,
                userPreferencesRepository = userPreferencesRepository,
                hasApiKey = hasApiKey,
            ) as T
        }
    }
}