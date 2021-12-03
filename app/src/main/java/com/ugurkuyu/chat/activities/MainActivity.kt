package com.ugurkuyu.chat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import com.ugurkuyu.chat.adapters.RecentConversationsAdapter
import com.ugurkuyu.chat.databinding.ActivityMainBinding
import com.ugurkuyu.chat.listeners.ConversationListener
import com.ugurkuyu.chat.models.ChatMessage
import com.ugurkuyu.chat.models.User
import com.ugurkuyu.chat.util.Constants
import com.ugurkuyu.chat.util.PreferenceManager

class MainActivity : AppCompatActivity(), ConversationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private var recentConversations: ArrayList<ChatMessage> = arrayListOf()
    private lateinit var recentConversationAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        preferenceManager = PreferenceManager(applicationContext)
        loadUserDetails()
        getToken()
        setListeners()
        listenConversations()
    }

    private fun init() {
        recentConversationAdapter = RecentConversationsAdapter(recentConversations, this)
        binding.recyclerViewRecentConv.adapter = recentConversationAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun setListeners() {
        binding.imageSignOut.setOnClickListener {
            signOut()
        }

        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(applicationContext, UsersActivity::class.java))
        }
    }

    private fun loadUserDetails() {
        binding.txtName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes: ByteArray =
            Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun listenConversations() {
        database.collection(Constants.KEY_COLLECT_CONVERSATIONS)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECT_CONVERSATIONS)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener =
        EventListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (error != null)
                return@EventListener
            if (value != null) {
                for (documentChange: DocumentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val senderId: String =
                            documentChange.document.getString(Constants.KEY_SENDER_ID).toString()
                        val receiverId: String =
                            documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString()
                        val chatMessage = ChatMessage()
                        chatMessage.senderId =
                            senderId
                        chatMessage.receiverId =
                            receiverId
                        if (preferenceManager.getString(Constants.KEY_USER_ID) == senderId) {
                            chatMessage.conversationImage =
                                documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)
                                    .toString()
                            chatMessage.conversationName =
                                documentChange.document.getString(Constants.KEY_RECEIVER_NAME)
                                    .toString()
                            chatMessage.conversationId =
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                                    .toString()
                        } else {
                            chatMessage.conversationImage =
                                documentChange.document.getString(Constants.KEY_SENDER_IMAGE)
                                    .toString()
                            chatMessage.conversationName =
                                documentChange.document.getString(Constants.KEY_SENDER_NAME)
                                    .toString()
                            chatMessage.conversationId =
                                documentChange.document.getString(Constants.KEY_SENDER_ID)
                                    .toString()
                        }
                        chatMessage.message =
                            documentChange.document.getString(Constants.KEY_LAST_MESSAGE).toString()
                        chatMessage.dateObject =
                            documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                        recentConversations.add(chatMessage)
                    } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                        for (i in recentConversations.indices) {
                            val senderId: String =
                                documentChange.document.getString(Constants.KEY_SENDER_ID)
                                    .toString()
                            val receiverId: String =
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                                    .toString()
                            if (recentConversations[i].senderId == senderId && recentConversations[i].receiverId == receiverId) {
                                recentConversations[i].message =
                                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                                        .toString()
                                recentConversations[i].dateObject =
                                    documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                                break
                            }
                        }
                    }

                }
            }
            recentConversations.sortWith(compareBy { it.dateObject })
            recentConversationAdapter.notifyDataSetChanged()
            binding.recyclerViewRecentConv.smoothScrollToPosition(0)
            binding.recyclerViewRecentConv.visibility = View.VISIBLE
            binding.progressBarRecentConv.visibility = View.GONE
        }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            updateToken(it)
        }
    }

    private fun updateToken(token: String) {
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference: DocumentReference =
            database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
        documentReference.update(Constants.KEY_FCM_TOKEN, token).addOnSuccessListener {
            showToast("Token updated successfully.")
        }.addOnFailureListener {
            showToast("Unable to update token.")
        }
    }

    private fun signOut() {
        showToast("Signing out...")
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference: DocumentReference =
            database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
        val updates: HashMap<String, Any> = hashMapOf()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates).addOnSuccessListener {
            preferenceManager.clear()
            startActivity(Intent(applicationContext, SignInActivity::class.java))
            finish()
        }.addOnFailureListener {
            showToast("Unable to sign out.")
        }
    }

    override fun onConversationClickedListener(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }
}