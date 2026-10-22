package com.example.chattingappv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chattingappv2.auth.AuthViewModel
import com.example.chattingappv2.chatdata.ChatViewModel
import com.example.chattingappv2.notification.RequestNotificationPermission
import com.example.chattingappv2.notification.createChatNotificationChannel
import com.example.chattingappv2.ui.screens.ChatScreen
import com.example.chattingappv2.ui.screens.HomeScreen
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
        createChatNotificationChannel(this)
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
                            RequestNotificationPermission()
                            // first() ném lỗi nếu đăng nhập bằng tài khoản ngoài 2 tài khoản thử, chấp nhận vì chỉ là code tạm
                            val otherUid = TEST_UIDS.first { it != currentUser.uid }
                            val chatViewModel: ChatViewModel = viewModel(
                                key = currentUser.uid,
                                factory = viewModelFactory { initializer { ChatViewModel(currentUser.uid, otherUid) } },
                                )
                            Column {
                                TextButton(onClick = viewModel::signOut) { Text("Đăng xuất") }
                                ChatScreen(chatViewModel, myUid = currentUser.uid, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}