package com.evilgpt.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilgpt.app.data.AiModels
import com.evilgpt.app.ui.theme.*
import com.evilgpt.app.viewmodel.AuthViewModel
import com.evilgpt.app.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    chatVm: ChatViewModel,
    authVm: AuthViewModel,
    onBack: () -> Unit
) {
    val chatState by chatVm.state.collectAsState()
    val authState by authVm.state.collectAsState()
    var apiKey by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf(chatState.systemPrompt) }
    var modelExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = EvilText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = EvilText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EvilSurface)
            )
        },
        containerColor = EvilBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Account", color = EvilGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = authState.user?.email ?: "Not signed in (local mode)",
                color = EvilTextMuted,
                fontSize = 13.sp
            )
            if (authState.isLoggedIn) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { authVm.signOut() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EvilError)
                ) { Text("Sign Out") }
            }

            Spacer(Modifier.height(24.dp))
            Text("OpenRouter API Key", color = EvilGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                placeholder = { Text("sk-or-v1-…", color = EvilTextMuted) },
                singleLine = true,
                colors = evilFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { chatVm.setApiKey(apiKey) },
                colors = ButtonDefaults.buttonColors(containerColor = EvilGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Save API Key") }
            Text(
                text = if (chatState.apiKeyConfigured) "Key configured ✓" else "No key saved yet",
                color = if (chatState.apiKeyConfigured) EvilGreen else EvilTextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(24.dp))
            Text("Default Model", color = EvilGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = modelExpanded, onExpandedChange = { modelExpanded = it }) {
                OutlinedTextField(
                    value = AiModels.ALL.find { it.id == chatState.selectedModelId }?.name ?: chatState.selectedModelId,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(modelExpanded) },
                    colors = evilFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = modelExpanded, onDismissRequest = { modelExpanded = false }) {
                    AiModels.ALL.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(model.name, color = EvilText)
                                    Text(model.description, color = EvilTextMuted, fontSize = 11.sp)
                                }
                            },
                            onClick = {
                                chatVm.setModel(model.id)
                                modelExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("System Prompt", color = EvilGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = systemPrompt,
                onValueChange = { systemPrompt = it },
                minLines = 5,
                maxLines = 12,
                colors = evilFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { chatVm.setSystemPrompt(systemPrompt) },
                colors = ButtonDefaults.buttonColors(containerColor = EvilGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Save Prompt") }
        }
    }
}

@Composable
private fun evilFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EvilGreen,
    unfocusedBorderColor = EvilBorder,
    focusedTextColor = EvilText,
    unfocusedTextColor = EvilText,
    cursorColor = EvilGreen,
    focusedLabelColor = EvilGreen,
    unfocusedLabelColor = EvilTextMuted
)
