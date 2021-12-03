package com.ugurkuyu.chat.listeners

import com.ugurkuyu.chat.models.User


interface ConversationListener {
    fun onConversationClickedListener(user: User)
}