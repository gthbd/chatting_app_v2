package com.example.chattingappv2.chatdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(val messages: List<Message> = emptyList(), val error: String? = null)

class ChatViewModel(
    private val myUid: String,
    otherUid: String,
    private val repository: ChatRepository = ChatRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val chatId = repository.chatIdOf(myUid, otherUid)

    init {
        launchGuarded {
            // Rules chỉ cho đọc tin khi members đã tồn tại, nên createChat phải chạy trước
            repository.createChat(myUid, otherUid)
            repository.observeMessages(chatId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun send(text: String) {
        if (text.isBlank()) return
        launchGuarded { repository.sendMessage(chatId, myUid, text.trim()) }
    }

    // Chỉ bắt DatabaseException để không nuốt CancellationException của coroutine
    private fun launchGuarded(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (e: DatabaseException) {
            _uiState.update { it.copy(error = e.message) }
        }
    }
}