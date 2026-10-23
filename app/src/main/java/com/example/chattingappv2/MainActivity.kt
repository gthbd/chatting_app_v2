package com.example.chattingappv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chattingappv2.auth.AuthViewModel
import com.example.chattingappv2.navigation.AppNavigation
import com.example.chattingappv2.ui.screens.LoginScreen
import com.example.chattingappv2.ui.theme.ChattingAppV2Theme

private val TEST_UIDS = listOf(
    "PQxgvOdjIdO7MOoztJcpRlpCnjt2",
    "y55VKJILeBZNS86VwyAPJJ1Grt62",
)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AuthViewModel = viewModel()
            val user by viewModel.currentUser.collectAsStateWithLifecycle()
            // Delegated property không smart-cast được, nên cần biến cục bộ
            val currentUser = user

            ChattingAppV2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.fillMaxSize().padding(innerPadding)) {
                        if (currentUser == null) {
                            LoginScreen(viewModel)
                        } else {
                            AppNavigation(
                                myUid = currentUser.uid,
                                myEmail = currentUser.email,
                                onSignOut = viewModel::signOut,
                            )
                        }
                    }
                }
            }
        }
    }
}