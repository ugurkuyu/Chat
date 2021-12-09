package com.ugurkuyu.chat.util

class Constants {
    companion object {
        const val KEY_COLLECTION_USERS: String = "users"
        const val KEY_NAME: String = "name"
        const val KEY_EMAIL: String = "email"
        const val KEY_PASSWORD: String = "password"
        const val KEY_PREFERENCE_NAME: String = "chatAppPreference"
        const val KEY_IS_SIGNED_IN: String = "isSignedIn"
        const val KEY_USER_ID: String = "userId"
        const val KEY_IMAGE: String = "image"
        const val KEY_FCM_TOKEN: String = "fcmToken"
        const val KEY_USER: String = "user"
        const val KEY_COLLECTION_CHAT: String = "chat"
        const val KEY_SENDER_ID: String = "senderId"
        const val KEY_RECEIVER_ID: String = "receiverId"
        const val KEY_MESSAGE: String = "message"
        const val KEY_TIMESTAMP: String = "timestamp"
        const val KEY_COLLECT_CONVERSATIONS: String = "conversations"
        const val KEY_SENDER_NAME: String = "senderName"
        const val KEY_RECEIVER_NAME: String = "receiverName"
        const val KEY_SENDER_IMAGE: String = "senderImage"
        const val KEY_RECEIVER_IMAGE: String = "receiverImage"
        const val KEY_LAST_MESSAGE: String = "lastMessage"
        const val KEY_USER_AVAILABILITY: String = "availability"
        const val REMOTE_MSG_AUTH: String = "Authorization"
        const val REMOTE_MSG_CONTENT_TYPE: String = "Content-Type"
        const val REMOTE_MSG_DATA: String = "data"
        const val REMOTE_MSG_REGISTRATION_IDS: String = "registration_ids"

        var remoteMsgHeaders: HashMap<String, String>? = null

        fun getRemoteMessageHeaders(): HashMap<String, String> {
            if (remoteMsgHeaders == null) {
                remoteMsgHeaders = hashMapOf()
                remoteMsgHeaders!![REMOTE_MSG_AUTH] =
                    "key=AAAAd8DFtiQ:APA91bHW07Z-QhRSX5nYZEokRlSzB7hEN5kmBeg2nxp5QlBsKEPBqdigPJR4pxCwC9oAu5Ry0QF32ur9IT5DnWRSlLDiMPX2-T1rzTnVSWYQbDltcFH2aW1gTBPIOQcuWwac6u-KsMZe"
                remoteMsgHeaders!![REMOTE_MSG_CONTENT_TYPE] = "application/json"
            }
            return remoteMsgHeaders!!
        }
    }
}