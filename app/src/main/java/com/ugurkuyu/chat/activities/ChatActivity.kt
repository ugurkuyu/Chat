package com.ugurkuyu.chat.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.ugurkuyu.chat.R
import com.ugurkuyu.chat.adapters.ChatAdapter
import com.ugurkuyu.chat.databinding.ActivityChatBinding
import com.ugurkuyu.chat.models.ChatMessage
import com.ugurkuyu.chat.models.User
import com.ugurkuyu.chat.util.Constants
import com.ugurkuyu.chat.util.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private var chatMessages: ArrayList<ChatMessage> = arrayListOf()
    private lateinit var adapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        adapter = ChatAdapter(
            getBitmapFromEncodedString(receiverUser.image),
            chatMessages,
            preferenceManager.getString(Constants.KEY_USER_ID)
        )
        binding.recyclerViewChat.adapter = adapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message: HashMap<String, Any> = hashMapOf()
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)
        message[Constants.KEY_RECEIVER_ID] = receiverUser.id
        message[Constants.KEY_MESSAGE] = binding.edtInputMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        binding.edtInputMessage.text = null
    }

    //private final EventListener<QuerySnapShot> eventListener = (value, error) -> {}

    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap {
        val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User
        binding.txtName.text = receiverUser.name
    }

    private fun setListeners() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun getReadableDateTime(date: Date) =
        SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
}