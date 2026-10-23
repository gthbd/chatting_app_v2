package com.example.chattingappv2.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.chattingappv2.chatdata.ChatViewModel
import com.example.chattingappv2.ui.screens.ChatListItem
import com.example.chattingappv2.ui.screens.ChatScreen
import com.example.chattingappv2.ui.screens.HomeScreen

// imports: Composable, viewModel, initializer, viewModelFactory, rememberViewModelStoreNavEntryDecorator,
// entryProvider, rememberNavBackStack, rememberSaveableStateHolderNavEntryDecorator, NavDisplay,
// ChatViewModel, ChatListItem, ChatScreen, HomeScreen

private val TEST_UIDS = listOf(
    "PQxgvOdjIdO7MOoztJcpRlpCnjt2",
    "y55VKJILeBZNS86VwyAPJJ1Grt62",
)

@Composable
fun AppNavigation(myUid: String, myEmail: String?, onSignOut: () -> Unit) {
    // Dữ liệu giả của bước N1; N3 thay bằng danh sách thật từ userChats
    require(myUid in TEST_UIDS) { "Tài khoản $myUid không nằm trong TEST_UIDS" }

    val backStack = rememberNavBackStack(HomeRoute)
    val chats = TEST_UIDS
        .filter { it != myUid }
        .map { ChatListItem(otherUid = it, title = "Tài khoản ${it.take(6)}…") }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    email = myEmail,
                    chats = chats,
                    onOpenChat = { otherUid -> backStack.add(ChatRoute(otherUid)) },
                    onSignOut = onSignOut,
                )
            }
            entry<ChatRoute> { route ->
                val chatViewModel: ChatViewModel = viewModel(
                    factory = viewModelFactory { initializer { ChatViewModel(myUid, route.otherUid) } },
                )
                ChatScreen(
                    viewModel = chatViewModel,
                    myUid = myUid,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}