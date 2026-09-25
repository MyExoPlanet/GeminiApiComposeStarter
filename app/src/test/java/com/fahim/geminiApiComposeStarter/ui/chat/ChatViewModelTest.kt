package com.fahim.geminiApiComposeStarter.ui.chat

import com.fahim.geminiApiComposeStarter.data.ChatHistoryDataSource
import com.fahim.geminiApiComposeStarter.data.ChatMessage
import com.fahim.geminiApiComposeStarter.data.ChatMessageEntity
import com.fahim.geminiApiComposeStarter.data.GeminiRepository
import com.fahim.geminiApiComposeStarter.data.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.test.advanceUntilIdle

private class FakeGeminiRepository(
    private val result: Result<String>,
) : GeminiRepository {

    var lastPrompt: String? = null

    override suspend fun generateText(
        prompt: String,
    ): Result<String> {
        lastPrompt = prompt
        return result
    }
}

private class FakeChatHistoryDataSource : ChatHistoryDataSource {

    private val messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())

    override fun getMessages(): Flow<List<ChatMessageEntity>> {
        return messages
    }

    override suspend fun saveMessage(message: ChatMessage) {
        messages.value = messages.value + ChatMessageEntity(
            id = message.id,
            text = message.text,
            sender = message.sender,
        )
    }

    override suspend fun clearHistory() {
        messages.value = emptyList()
    }
}

private class FakeUserPreferencesDataSource : UserPreferencesDataSource {

    private val timestamps = MutableStateFlow(false)

    override val showTimestamps: Flow<Boolean> = timestamps

    override suspend fun setShowTimestamps(enabled: Boolean) {
        timestamps.value = enabled
    }
}

class ChatViewModelTest {

    @Test
    fun emptyPromptShowsValidationError() = runTest {
        val viewModel = createViewModel()

        viewModel.onSend()

        assertEquals(
            PromptError.EMPTY,
            viewModel.uiState.value.promptError,
        )
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun successfulPromptAddsUserAndGeminiMessages() = runTest {
        val repository = FakeGeminiRepository(
            Result.success("Hello from Gemini"),
        )
        val history = FakeChatHistoryDataSource()
        val viewModel = createViewModel(
            repository = repository,
            history = history,
        )

        viewModel.onPromptChange("Hello Gemini")
        viewModel.onSend()

        advanceUntilIdle()

        assertEquals(
            "Hello Gemini",
            repository.lastPrompt,
        )

        assertEquals(
            2,
            viewModel.uiState.value.messages.size,
        )

        assertEquals(
            "USER",
            viewModel.uiState.value.messages[0].sender,
        )

        assertEquals(
            "Hello Gemini",
            viewModel.uiState.value.messages[0].text,
        )

        assertEquals(
            "GEMINI",
            viewModel.uiState.value.messages[1].sender,
        )

        assertEquals(
            "Hello from Gemini",
            viewModel.uiState.value.messages[1].text,
        )

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun failedPromptShowsErrorMessage() = runTest {
        val repository = FakeGeminiRepository(
            Result.failure(
                IllegalStateException("Network error"),
            ),
        )

        val viewModel = createViewModel(
            repository = repository,
        )

        viewModel.onPromptChange("Hello Gemini")
        viewModel.onSend()

        advanceUntilIdle()

        assertEquals(
            "Network error",
            viewModel.uiState.value.errorMessage,
        )

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun missingApiKeyShowsErrorWithoutCallingGemini() = runTest {
        val repository = FakeGeminiRepository(
            Result.success("Should not be called"),
        )

        val viewModel = createViewModel(
            repository = repository,
            hasApiKey = false,
        )

        viewModel.onPromptChange("Hello Gemini")
        viewModel.onSend()

        advanceUntilIdle()

        assertEquals(
            ChatViewModel.MISSING_API_KEY_MESSAGE,
            viewModel.uiState.value.errorMessage,
        )

        assertEquals(
            null,
            repository.lastPrompt,
        )

        assertTrue(
            viewModel.uiState.value.messages.isEmpty(),
        )
    }

    private fun createViewModel(
        repository: GeminiRepository = FakeGeminiRepository(
            Result.success("Test response"),
        ),
        history: FakeChatHistoryDataSource = FakeChatHistoryDataSource(),
        preferences: FakeUserPreferencesDataSource =
            FakeUserPreferencesDataSource(),
        hasApiKey: Boolean = true,
    ): ChatViewModel {
        return ChatViewModel(
            repository = repository,
            chatHistoryRepository = history,
            userPreferencesRepository = preferences,
            hasApiKey = hasApiKey,
        )
    }
}