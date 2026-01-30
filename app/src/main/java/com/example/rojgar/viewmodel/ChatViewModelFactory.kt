package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rojgar.repository.ChatRepository
import com.example.rojgar.repository.GroupChatRepository

class ChatViewModelFactory(private val chatRepository: ChatRepository,
                           private val groupChatRepository: GroupChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, groupChatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
