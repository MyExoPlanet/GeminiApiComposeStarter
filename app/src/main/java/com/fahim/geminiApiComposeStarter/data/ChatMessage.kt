package com.fahim.geminiApiComposeStarter.data

data class ChatMessage(
    val id: Long,
    val text: String,
    val sender: String,
)