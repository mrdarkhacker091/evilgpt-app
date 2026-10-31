package com.evilgpt.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilgpt.app.data.AiModels
import com.evilgpt.app.data.ChatMessage
import com.evilgpt.app.ui.theme.*
import com.evilgpt.app.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatVm: ChatViewModel,
    onOpenSettings: () -> Unit
) {
    val state by chatVm.state.collectAsState()
    var input by remember { mutableStateOf("") }
    var drawerOpen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    LaunchedEffect(state.currentSession?.messages?.size, state.streamingText) {
        val count = (state.currentSession?.messages?.size ?: 0) + if (state.streamingText.isNotEmpty()) 1 else 0
        if (count > 0) {
            scope.launch { listState.animateScrollToItem(count - 1) }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = EvilSurface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(Modifier.fillMaxSize().padding(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chats", color = EvilGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = {
                            chatVm.newChat()
                            scope.launch { drawerState.close() }
                        }) {
                            Icon(Icons.Default.Add, null, tint = EvilGreen)
                        }
                    }
                    HorizontalDivider(color = EvilBorder, modifier = Modifier.padding(vertical = 8.dp))
                    LazyColumn(Modifier.weight(1f)) {
                        items(state.sessions, key = { it.id }) { session ->
                            val selected = session.id == state.currentSession?.id
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (selected) EvilSurfaceVariant else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        chatVm.selectSession(session.id)
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ChatBubbleOutline, null, tint = EvilTextMuted, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    session.title.ifBlank { "New Chat" },
                                    color = EvilText,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { chatVm.deleteSession(session.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = EvilTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = EvilBorder)
                    TextButton(
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenSettings()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, null, tint = EvilTextMuted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Settings", color = EvilText)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                state.currentSession?.title?.ifBlank { "EvilGPT" } ?: "EvilGPT",
                                color = EvilText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                AiModels.ALL.find { it.id == state.selectedModelId }?.name ?: state.selectedModelId,
                                color = EvilTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null, tint = EvilText)
                        }
                    },
                    actions = {
                        if (state.isStreaming) {
                            IconButton(onClick = { chatVm.stopStreaming() }) {
                                Icon(Icons.Default.Stop, null, tint = EvilError)
                            }
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, null, tint = EvilTextMuted)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = EvilSurface)
                )
            },
            containerColor = EvilBg
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                state.error?.let { err ->
                    Surface(color = EvilError.copy(alpha = 0.15f), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(err, color = EvilError, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { chatVm.clearError() }) {
                                Icon(Icons.Default.Close, null, tint = EvilError, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                val messages = state.currentSession?.messages ?: emptyList()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (messages.isEmpty() && state.streamingText.isEmpty()) {
                        item {
                            Column(
                                Modifier.fillMaxWidth().padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("EvilGPT", color = EvilGreen, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Uncensored. Unfiltered. No refusals.",
                                    color = EvilTextMuted,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(msg)
                    }
                    if (state.streamingText.isNotEmpty()) {
                        item {
                            MessageBubble(
                                ChatMessage(role = "assistant", content = state.streamingText, isStreaming = true)
                            )
                        }
                    }
                }

                // Input bar
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(EvilSurface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Message EvilGPT…", color = EvilTextMuted) },
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EvilGreen,
                            unfocusedBorderColor = EvilBorder,
                            focusedTextColor = EvilText,
                            unfocusedTextColor = EvilText,
                            cursorColor = EvilGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (input.isNotBlank() && !state.isStreaming) {
                                chatVm.sendMessage(input)
                                input = ""
                            }
                        },
                        containerColor = EvilGreen,
                        contentColor = Color.Black,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (state.isStreaming) {
                            CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) UserBubble else AssistantBubble,
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 14.dp
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                if (!isUser) {
                    Text("EvilGPT", color = EvilGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = msg.content + if (msg.isStreaming) " ▍" else "",
                    color = EvilText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
