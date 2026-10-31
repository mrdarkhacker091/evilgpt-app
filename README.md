# EvilGPT — Native Android

100% native Kotlin + Jetpack Compose rebuild of EvilGPT.

## Features
- Supabase Auth (email/password) + local skip-login mode
- OpenRouter streaming chat (SSE)
- Multiple uncensored models
- Chat history (DataStore)
- System prompt customization
- Dark green EvilGPT theme
- Drawer navigation, settings, model picker

## Requirements
- Android Studio Ladybug+ / AGP 8.7
- JDK 17
- minSdk 23

## Build
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Config
1. Open Settings in the app
2. Paste your OpenRouter API key (`sk-or-v1-…`)
3. Optionally change model / system prompt

Supabase project: `nhlrpjlhgzbkhnvstshf` (same as original web app).

## Architecture
- MVVM + Kotlin Coroutines / Flow
- Retrofit/OkHttp for OpenRouter SSE streaming
- Supabase-kt for auth
- Jetpack Compose Material3 UI
- DataStore for local sessions & secrets

Not a PWA/TWA/WebView. Fully native.
