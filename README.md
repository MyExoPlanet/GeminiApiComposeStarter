# Gemini API Compose Starter

A Jetpack Compose Android application developed for Mobile Application Development Assignment 1.

The application provides a Gemini-powered conversational interface with secure API-key handling, persistent conversation history, user preferences, voice input, adaptive UI, and automated tests.

## Features

- Gemini API integration
- Jetpack Compose Material 3 chat interface
- User and Gemini chat bubbles
- Loading indicator during Gemini requests
- Error handling with Snackbar
- Automatic chat scrolling
- Persistent conversation history using Room
- Show/hide message timestamps using Preferences DataStore
- Voice input using Android speech recognition
- Responsive UI using Material 3 WindowSizeClass
- AES-256-GCM encryption using Android Keystore
- R8 code shrinking and resource shrinking for release builds
- ChatViewModel unit tests
- Jetpack Compose UI test

## Requirements

- Android Studio
- Android SDK
- A configured Android emulator or physical Android device
- Gemini API key from Google AI Studio

## API Key Setup

The Gemini API key must never be hardcoded into Kotlin source files, XML resources, or Gradle files.

Create a `local.properties` file in the project root and add:

```properties
GEMINI_API_KEY=your_api_key_here