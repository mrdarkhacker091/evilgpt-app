package com.evilgpt.app.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" | "assistant" | "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)


data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val messages: List<ChatMessage> = emptyList(),
    val modelId: String = AiModels.DEFAULT.id,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AiModel(
    val id: String,
    val name: String,
    val description: String,
    val contextWindow: String = "128K"
)

object AiModels {
    val DEFAULT = AiModel(
        id = "openai/gpt-4o-mini",
        name = "GPT-4o Mini",
        description = "Fast general purpose",
        contextWindow = "128K"
    )
    val ALL = listOf(
        DEFAULT,
        AiModel("openai/gpt-4o", "GPT-4o", "Flagship multimodal", "128K"),
        AiModel("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet", "Strong reasoning", "200K"),
        AiModel("meta-llama/llama-3.1-70b-instruct", "Llama 3.1 70B", "Open weights", "128K"),
        AiModel("deepseek/deepseek-chat", "DeepSeek Chat", "Coding focused", "64K"),
        AiModel("mistralai/mistral-large", "Mistral Large", "European flagship", "128K"),
        AiModel("qwen/qwen-2.5-72b-instruct", "Qwen 2.5 72B", "Long context", "128K"),
        AiModel("google/gemini-pro-1.5", "Gemini 1.5 Pro", "Google multimodal", "1M"),
        AiModel("x-ai/grok-beta", "Grok Beta", "xAI", "128K")
    )
}


data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val stream: Boolean = true,
    @Json(name = "max_tokens") val maxTokens: Int = 2000,
    val temperature: Double = 0.7
)


data class OpenRouterMessage(
    val role: String,
    val content: String
)


data class OpenRouterChunk(
    val id: String? = null,
    val choices: List<Choice>? = null
) {
    
    data class Choice(
        val delta: Delta? = null,
        val index: Int? = null,
        @Json(name = "finish_reason") val finishReason: String? = null
    )
    
    data class Delta(
        val role: String? = null,
        val content: String? = null
    )
}


data class AiConfiguration(
    @Json(name = "free_system_prompt") val freeSystemPrompt: String? = null,
    @Json(name = "max_tokens") val maxTokens: Int? = null
)

data class UserProfile(
    val id: String,
    val email: String?,
    val displayName: String? = null
)
