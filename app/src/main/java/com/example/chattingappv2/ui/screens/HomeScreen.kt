package com.example.chattingappv2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ChatListItem(val otherUid: String, val title: String)

@Composable
fun HomeScreen(
    email: String?,
    chats: List<ChatListItem>,
    onOpenChat: (otherUid: String) -> Unit,
    onSignOut: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Đã đăng nhập: $email", modifier = Modifier.weight(1f))
            TextButton(onClick = onSignOut) { Text("Đăng xuất") }
        }
        LazyColumn(Modifier.weight(1f)) {
            items(chats, key = { it.otherUid }) { chat ->
                ListItem(
                    headlineContent = { Text(chat.title) },
                    modifier = Modifier.clickable { onOpenChat(chat.otherUid) },
                )
                HorizontalDivider()
            }
        }
    }
}