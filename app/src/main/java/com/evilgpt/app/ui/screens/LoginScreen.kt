package com.evilgpt.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilgpt.app.ui.theme.*
import com.evilgpt.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authVm: AuthViewModel,
    onLoggedIn: () -> Unit
) {
    val state by authVm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EvilBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Text(
                text = "EvilGPT",
                color = EvilGreen,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Uncensored AI Chat",
                color = EvilTextMuted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = evilTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = evilTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let { err ->
                Text(
                    text = err,
                    color = EvilError,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (isRegister) authVm.signUp(email.trim(), password)
                    else authVm.signIn(email.trim(), password)
                },
                enabled = !state.isLoading && email.isNotBlank() && password.length >= 6,
                colors = ButtonDefaults.buttonColors(containerColor = EvilGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text(if (isRegister) "Create Account" else "Sign In", fontWeight = FontWeight.SemiBold)
                }
            }

            TextButton(onClick = { isRegister = !isRegister; authVm.clearError() }) {
                Text(
                    text = if (isRegister) "Already have an account? Sign in" else "No account? Register",
                    color = EvilTextMuted,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "or continue without account (local chats)",
                color = EvilGreenDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            TextButton(onClick = onLoggedIn) {
                Text("Skip Login", color = EvilGreen, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun evilTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EvilGreen,
    unfocusedBorderColor = EvilBorder,
    focusedLabelColor = EvilGreen,
    unfocusedLabelColor = EvilTextMuted,
    cursorColor = EvilGreen,
    focusedTextColor = EvilText,
    unfocusedTextColor = EvilText
)
