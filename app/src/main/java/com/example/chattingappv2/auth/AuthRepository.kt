package com.example.chattingappv2.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),    //getInstance(): trả về một thể hiện duy nhất của FirebaseAuth
    //FirebaseAuth: là kiểu dữ liệu biểu diễn một đối tượng xác thực Firebase
) {
    // Đọc đồng bộ để UI không nháy màn Login trước khi listener phát giá trị đầu
    val currentUserNow: FirebaseUser? get() = auth.currentUser

    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }   //awaitClose(): Hủy bỏ listener khi coroutine bị hủy
    }

    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun signOut() = auth.signOut()
}