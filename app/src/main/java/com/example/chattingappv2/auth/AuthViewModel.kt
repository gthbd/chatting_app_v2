package com.example.chattingappv2.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattingappv2.notification.PushTokenRepository
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class AuthUiState(val isLoading: Boolean = false, val error: String? = null)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val pushTokens: PushTokenRepository = PushTokenRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = repository.currentUser.stateIn(     //stateIn(): chuyển một Flow thành StateFlow
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = repository.currentUserNow,
    )

    fun signIn(email: String, password: String) = submit(email, password, repository::signIn)

    fun signUp(email: String, password: String) = submit(email, password, repository::signUp)

    init {
        viewModelScope.launch {
            // Token có thể đã đổi lúc app không chạy, nên đồng bộ ở mỗi lần đăng nhập và mỗi lần mở app
            repository.currentUser.filterNotNull().collect { user ->
                bestEffort { pushTokens.register(user.uid) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.currentUserNow?.let { user ->
                // Phải xóa lúc còn đăng nhập, vì rules chỉ cho chủ tài khoản xóa token của mình
                bestEffort { pushTokens.unregister(user.uid) }
            }
            repository.signOut()
        }
    }
    // Lỗi token không được chặn đăng nhập hay đăng xuất nên chỉ ghi log
    private suspend fun bestEffort(action: suspend () -> Unit) {
        try {
            action()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("AuthViewModel", "Push token sync failed", e)
        }
    }

    private fun submit(
        email: String,
        password: String,
        action: suspend (String, String) -> Unit,
    ) {
        if (_uiState.value.isLoading) return
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Nhập email và mật khẩu")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            // Chỉ bắt FirebaseException để không nuốt CancellationException của coroutine
            _uiState.value = try {
                action(email.trim(), password)
                AuthUiState()
            } catch (e: FirebaseException) {
                AuthUiState(error = e.toUserMessage())
            }
        }
    }
}

// WeakPassword kế thừa InvalidCredentials, nên phải đứng trước
private fun FirebaseException.toUserMessage(): String = when (this) {
    is FirebaseNetworkException -> "Không có kết nối mạng"
    is FirebaseAuthWeakPasswordException -> "Mật khẩu tối thiểu 6 ký tự"
    is FirebaseAuthUserCollisionException -> "Email đã được đăng ký"
    is FirebaseAuthInvalidCredentialsException,
    is FirebaseAuthInvalidUserException -> "Email hoặc mật khẩu không đúng"
    else -> "Lỗi: $message"
}