package com.ugurkuyu.chat.firebase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ugurkuyu.chat.R
import com.ugurkuyu.chat.activities.ChatActivity
import com.ugurkuyu.chat.models.User
import com.ugurkuyu.chat.util.Constants
import kotlin.random.Random

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val user = User()
        user.id = remoteMessage.data[Constants.KEY_USER_ID].toString()
        user.name = remoteMessage.data[Constants.KEY_NAME].toString()
        user.token = remoteMessage.data[Constants.KEY_FCM_TOKEN].toString()

        val notificationId = Random(0).nextInt()
        val channelId = "chat_message"

        val intent = Intent(this, ChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(Constants.KEY_USER, user)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        notificationBuilder.apply {
            setSmallIcon(R.drawable.ic_notifications_)
            setContentTitle(user.name)
            setContentText(remoteMessage.data[Constants.KEY_MESSAGE])
            setStyle(
                NotificationCompat.BigTextStyle().bigText(remoteMessage.data[Constants.KEY_MESSAGE])
            )
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(pendingIntent)
            setAutoCancel(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelName = "Chat Message" as CharSequence
                val channelDescription =
                    "This notification channel is used for chat message notifications"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(channelId, channelName, importance)
                channel.description = channelDescription
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }

            val notificationManagerCompat = NotificationManagerCompat.from(this@MessagingService)
            notificationManagerCompat.notify(notificationId, notificationBuilder.build())

        }
    }

}