package com.example.chattingappv2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chattingappv2.chatdata.ChatViewModel
import com.example.chattingappv2.chatdata.Message
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    myUid: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Cuộn xuống tin mới nhất mỗi khi danh sách dài thêm
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(modifier.fillMaxSize().imePadding()) {
        TextButton(onClick = onBack) { Text("‹ Quay lại") }
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageBubble(message, isMine = message.senderUid == myUid)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text("Nhắn tin") },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { viewModel.send(draft); draft = "" },
                enabled = draft.isNotBlank(),
            ) { Text("Gửi") }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isMine: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Surface(
            color = if (isMine) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(message.text)
                Text(
                    text = formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(if (isMine) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    
    val messageCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val nowCalendar = Calendar.getInstance()
    
    val sameDay = messageCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) &&
                  messageCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR)
                  
    val pattern = if (sameDay) {
        "HH:mm" // Hôm nay chỉ hiện giờ:phút
    } else {
        "dd/MM/yyyy HH:mm" // Khác ngày thì hiện thêm ngày tháng
    }
    
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
}