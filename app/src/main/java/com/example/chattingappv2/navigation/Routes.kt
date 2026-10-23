package com.example.chattingappv2.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data class ChatRoute(val otherUid: String) : NavKey