package com.ugurkuyu.chat.models

import java.io.Serializable

data class User(
    var name: String = "",
    var image: String? = null,
    var email: String = "",
    var token: String? = null,
    var id: String = ""
) : Serializable {
}