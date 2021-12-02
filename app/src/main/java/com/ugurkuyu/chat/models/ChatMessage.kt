package com.ugurkuyu.chat.models

import java.io.Serializable
import java.util.*

data class ChatMessage(
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = "",
    var dateTime: String = "",
    var dateObject: Date? = null
) : Serializable
