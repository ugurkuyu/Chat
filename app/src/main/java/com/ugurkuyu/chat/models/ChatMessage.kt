package com.ugurkuyu.chat.models

import java.io.Serializable

data class ChatMessage(
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = "",
    var dateTime: String = ""
) : Serializable
