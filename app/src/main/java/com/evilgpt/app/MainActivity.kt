package com.evilgpt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.evilgpt.app.ui.screens.ChatScreen
import com.evilgpt.app.ui.screens.LoginScreen
import com.evilgpt.app.ui.screens.SettingsScreen
import com.evilgpt.app.ui.theme.EvilBg
import com.evilgpt.app.ui.theme.EvilGptTheme
import com.evilgpt.app.viewmodel.AuthViewModel
import com.evilgpt.app.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvilGptTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = EvilBg) {
                    EvilGptNav()
                }
            }
        }
    }
}

@Composable
private fun EvilGptNav() {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val chatVm: ChatViewModel = viewModel()
    var showLogin by remember { mutableStateOf(true) }

    if (showLogin) {
        LoginScreen(
            authVm = authVm,
            onLoggedIn = { showLogin = false }
        )
    } else {
        NavHost(navController = nav, startDestination = "chat") {
            composable("chat") {
                ChatScreen(
                    chatVm = chatVm,
                    onOpenSettings = { nav.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    chatVm = chatVm,
                    authVm = authVm,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
