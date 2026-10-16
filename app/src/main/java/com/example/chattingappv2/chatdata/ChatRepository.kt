package com.example.chattingappv2.chatdata

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class Message(
    val id: String = "",
    val senderUid: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
)

class ChatRepository(
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
) {
    private val chatsRef = database.getReference("chats")
    private val messagesRef = database.getReference("messages")

    fun chatIdOf(uidA: String, uidB: String): String =
        listOf(uidA, uidB).sorted().joinToString("_")

    suspend fun createChat(myUid: String, otherUid: String): String {
        require(myUid != otherUid) { "Cannot chat with yourself" }

        val chatId = chatIdOf(myUid, otherUid)
        chatsRef.child(chatId).child("members")
            .setValue(mapOf(myUid to true, otherUid to true))
            .await()
        return chatId
    }

    suspend fun sendMessage(chatId: String, senderUid: String, text: String) {
        require(text.isNotBlank()) { "Message text must not be blank" }

        messagesRef.child(chatId).push()
            .setValue(
                mapOf(
                    "senderUid" to senderUid,
                    "text" to text,
                    "timestamp" to ServerValue.TIMESTAMP,
                )
            )
            .await()
    }

    fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val messages = mutableListOf<Message>()
        val query = messagesRef.child(chatId).orderByKey().limitToLast(PAGE_SIZE)

        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java) ?: return
                messages += message.copy(id = snapshot.key.orEmpty())
                trySend(messages.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onChildRemoved(snapshot: DataSnapshot) = Unit
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
        }

        query.addChildEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}