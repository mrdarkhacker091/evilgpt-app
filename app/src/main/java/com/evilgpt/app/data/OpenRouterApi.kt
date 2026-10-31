package com.evilgpt.app.data

import com.evilgpt.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.TimeUnit

class OpenRouterApi(
    private val apiKeyProvider: () -> String?
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val requestAdapter = moshi.adapter(OpenRouterRequest::class.java)
    private val chunkAdapter = moshi.adapter(OpenRouterChunk::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    fun streamChat(
        model: String,
        messages: List<OpenRouterMessage>,
        systemPrompt: String,
        maxTokens: Int = 2000
    ): Flow<String> = flow {
        val key = apiKeyProvider()
            ?: throw IllegalStateException("OpenRouter API key not configured")

        val fullMessages = listOf(
            OpenRouterMessage(role = "system", content = systemPrompt)
        ) + messages

        val body = OpenRouterRequest(
            model = model,
            messages = fullMessages,
            stream = true,
            maxTokens = maxTokens
        )
        val json = requestAdapter.toJson(body)

        val request = Request.Builder()
            .url("${BuildConfig.OPENROUTER_BASE}/chat/completions")
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://evil-gpt-sepia.vercel.app")
            .addHeader("X-Title", "EvilGPT Android")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val err = response.body?.string()?.take(300) ?: "unknown"
                throw RuntimeException("OpenRouter ${response.code}: $err")
            }
            val source = response.body?.source() ?: return@use
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break
                    try {
                        val chunk = chunkAdapter.fromJson(data)
                        val delta = chunk?.choices?.firstOrNull()?.delta?.content
                        if (!delta.isNullOrEmpty()) emit(delta)
                    } catch (_: Exception) {
                        // skip malformed SSE lines
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
