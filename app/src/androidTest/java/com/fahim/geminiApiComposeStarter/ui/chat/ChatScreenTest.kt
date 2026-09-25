package com.fahim.geminiApiComposeStarter.ui.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.fahim.geminiApiComposeStarter.data.ChatMessage
import org.junit.Rule
import org.junit.Test

class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun chatMessagesAreDisplayed() {
        composeTestRule.setContent {
            ChatScreen(
                state = ChatUiState(
                    messages = listOf(
                        ChatMessage(
                            id = 1L,
                            text = "Hello from me",
                            sender = "USER",
                        ),
                        ChatMessage(
                            id = 2L,
                            text = "Hello from Gemini",
                            sender = "GEMINI",
                        ),
                    ),
                ),
                onPromptChange = {},
                onSend = {},
                onShowTimestampsChange = {},
                windowSizeClass = WindowSizeClass.calculateFromSize(
                    DpSize(360.dp, 800.dp),
                ),
            )
        }

        composeTestRule
            .onNodeWithText("Hello from me")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Hello from Gemini")
            .assertIsDisplayed()
    }
}