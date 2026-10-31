package com.evilgpt.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evilgpt.app.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val sessions: List<ChatSession> = emptyList(),
    val currentSession: ChatSession? = null,
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val error: String? = null,
    val selectedModelId: String = AiModels.DEFAULT.id,
    val systemPrompt: String = ChatRepository.DEFAULT_SYSTEM_PROMPT,
    val apiKeyConfigured: Boolean = false
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val chatRepo = ChatRepository(app)
    private val openRouter = OpenRouterApi { _apiKey }

    private var _apiKey: String? = null
    private var streamJob: Job? = null

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                chatRepo.sessions,
                chatRepo.selectedModel,
                chatRepo.systemPrompt,
                chatRepo.apiKey
            ) { sessions, model, prompt, key ->
                Triple(sessions, model to prompt, key)
            }.collect { (sessions, modelPrompt, key) ->
                _apiKey = key
                val currentId = _state.value.currentSession?.id
                val current = sessions.find { it.id == currentId } ?: sessions.firstOrNull()
                _state.update {
                    it.copy(
                        sessions = sessions,
                        currentSession = current,
                        selectedModelId = modelPrompt.first,
                        systemPrompt = modelPrompt.second,
                        apiKeyConfigured = !key.isNullOrBlank()
                    )
                }
            }
        }
    }

    fun selectSession(id: String) {
        val s = _state.value.sessions.find { it.id == id } ?: return
        _state.update { it.copy(currentSession = s, streamingText = "", error = null) }
    }

    fun newChat() {
        viewModelScope.launch {
            val s = chatRepo.createSession(_state.value.selectedModelId)
            _state.update { it.copy(currentSession = s, streamingText = "", error = null) }
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            chatRepo.deleteSession(id)
            if (_state.value.currentSession?.id == id) {
                _state.update { it.copy(currentSession = it.sessions.firstOrNull { sess -> sess.id != id }) }
            }
        }
    }

    fun setModel(modelId: String) {
        viewModelScope.launch {
            chatRepo.saveSelectedModel(modelId)
            val cur = _state.value.currentSession
            if (cur != null) {
                chatRepo.upsertSession(cur.copy(modelId = modelId))
            }
        }
    }

    fun setApiKey(key: String) {
        viewModelScope.launch { chatRepo.saveApiKey(key.trim()) }
    }

    fun setSystemPrompt(prompt: String) {
        viewModelScope.launch { chatRepo.saveSystemPrompt(prompt) }
    }

    fun sendMessage(text: String) {
        val content = text.trim()
        if (content.isEmpty() || _state.value.isStreaming) return
        if (_apiKey.isNullOrBlank()) {
            _state.update { it.copy(error = "Set your OpenRouter API key in Settings") }
            return
        }

        viewModelScope.launch {
            var session = _state.value.currentSession
            if (session == null) {
                session = chatRepo.createSession(_state.value.selectedModelId)
            }

            val userMsg = ChatMessage(role = "user", content = content)
            val updatedMessages = session.messages + userMsg
            val title = if (session.messages.isEmpty()) content.take(40) else session.title
            session = session.copy(
                messages = updatedMessages,
                title = title,
                updatedAt = System.currentTimeMillis()
            )
            chatRepo.upsertSession(session)
            _state.update {
                it.copy(
                    currentSession = session,
                    isStreaming = true,
                    streamingText = "",
                    error = null
                )
            }

            val history = updatedMessages
                .filter { it.role == "user" || it.role == "assistant" }
                .takeLast(40)
                .map { OpenRouterMessage(it.role, it.content) }

            val model = session.modelId.ifBlank { _state.value.selectedModelId }
            val sb = StringBuilder()

            streamJob?.cancel()
            streamJob = viewModelScope.launch {
                try {
                    openRouter.streamChat(
                        model = model,
                        messages = history,
                        systemPrompt = _state.value.systemPrompt
                    ).collect { delta ->
                        sb.append(delta)
                        _state.update { it.copy(streamingText = sb.toString()) }
                    }
                    val assistantMsg = ChatMessage(role = "assistant", content = sb.toString())
                    val finalSession = session.copy(
                        messages = session.messages + assistantMsg,
                        updatedAt = System.currentTimeMillis()
                    )
                    chatRepo.upsertSession(finalSession)
                    _state.update {
                        it.copy(
                            currentSession = finalSession,
                            isStreaming = false,
                            streamingText = ""
                        )
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            isStreaming = false,
                            error = e.message ?: "Stream failed",
                            streamingText = ""
                        )
                    }
                }
            }
        }
    }

    fun stopStreaming() {
        streamJob?.cancel()
        val partial = _state.value.streamingText
        if (partial.isNotBlank()) {
            viewModelScope.launch {
                val session = _state.value.currentSession ?: return@launch
                val assistantMsg = ChatMessage(role = "assistant", content = partial)
                val finalSession = session.copy(messages = session.messages + assistantMsg)
                chatRepo.upsertSession(finalSession)
                _state.update {
                    it.copy(
                        currentSession = finalSession,
                        isStreaming = false,
                        streamingText = ""
                    )
                }
            }
        } else {
            _state.update { it.copy(isStreaming = false, streamingText = "") }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
