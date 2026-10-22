package com.example.chattingappv2.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.chattingappv2.R
import kotlin.jvm.java

fun createChatNotificationChannel(context: Context) {
    // NotificationChannel chỉ có từ API 26
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val channel = NotificationChannel(
        context.getString(R.string.chat_channel_id),
        "Tin nhắn",
        NotificationManager.IMPORTANCE_HIGH,
    )
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}

@Composable
fun RequestNotificationPermission() {
    // Dưới Android 13 quyền được cấp sẵn
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
}