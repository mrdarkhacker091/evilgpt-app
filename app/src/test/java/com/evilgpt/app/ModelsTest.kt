package com.evilgpt.app

import com.evilgpt.app.data.AiModels
import com.evilgpt.app.data.ChatMessage
import com.evilgpt.app.data.ChatSession
import org.junit.Assert.*
import org.junit.Test

class ModelsTest {
    @Test
    fun defaultModelExists() {
        assertTrue(AiModels.ALL.any { it.id == AiModels.DEFAULT.id })
    }

    @Test
    fun chatMessageDefaults() {
        val m = ChatMessage(role = "user", content = "hi")
        assertEquals("user", m.role)
        assertTrue(m.id.isNotBlank())
        assertFalse(m.isStreaming)
    }

    @Test
    fun chatSessionDefaults() {
        val s = ChatSession()
        assertEquals("New Chat", s.title)
        assertTrue(s.messages.isEmpty())
        assertEquals(AiModels.DEFAULT.id, s.modelId)
    }

    @Test
    fun modelsHaveUniqueIds() {
        val ids = AiModels.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
