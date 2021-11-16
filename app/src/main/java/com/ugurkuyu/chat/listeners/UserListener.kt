package com.ugurkuyu.chat.listeners

import com.ugurkuyu.chat.models.User

interface UserListener {
    fun onUserClicked(user: User)
}