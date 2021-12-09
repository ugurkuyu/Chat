package com.ugurkuyu.chat.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.ugurkuyu.chat.adapters.ChatAdapter
import com.ugurkuyu.chat.databinding.ActivityChatBinding
import com.ugurkuyu.chat.models.ChatMessage
import com.ugurkuyu.chat.models.User
import com.ugurkuyu.chat.network.ApiClient
import com.ugurkuyu.chat.network.ApiService
import com.ugurkuyu.chat.util.Constants
import com.ugurkuyu.chat.util.PreferenceManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private var chatMessages: ArrayList<ChatMessage> = arrayListOf()
    private lateinit var adapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    private var conversationId: String? = null
    private var isReceiverAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
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
        if (conversationId != null)
            updateConversation(binding.edtInputMessage.text.toString())
        else {
            val conversation: HashMap<String, Any> = hashMapOf()
            conversation[Constants.KEY_SENDER_ID] =
                preferenceManager.getString(Constants.KEY_USER_ID)
            conversation[Constants.KEY_SENDER_NAME] =
                preferenceManager.getString(Constants.KEY_NAME)
            conversation[Constants.KEY_SENDER_IMAGE] =
                preferenceManager.getString(Constants.KEY_IMAGE)
            conversation[Constants.KEY_RECEIVER_ID] = receiverUser.id
            conversation[Constants.KEY_RECEIVER_NAME] = receiverUser.name
            conversation[Constants.KEY_RECEIVER_IMAGE] = receiverUser.image!!
            conversation[Constants.KEY_LAST_MESSAGE] = binding.edtInputMessage.text.toString()
            conversation[Constants.KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }
        if (!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(receiverUser.token)

                val data = JSONObject()
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
                data.put(
                    Constants.KEY_FCM_TOKEN,
                    preferenceManager.getString(Constants.KEY_FCM_TOKEN)
                )
                data.put(Constants.KEY_MESSAGE, binding.edtInputMessage.text)

                val body = JSONObject()
                body.put(Constants.REMOTE_MSG_DATA, data)
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

                sendNotification(body.toString())

            } catch (e: Exception) {
                showToast("hataaa: " +e.message.toString())
            }
        }
        binding.edtInputMessage.text = null
    }

    private fun sendNotification(messageBody: String) {
        ApiClient.retrofit.create(ApiService::class.java)
            .sendMessage(Constants.getRemoteMessageHeaders(), messageBody)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        try {
                            response.body().let {
                                val responseJson = JSONObject(response.body()!!)
                                val results = responseJson.getJSONArray("results")
                                if (responseJson.getInt("failure") == 1) {
                                    val error = results[0] as JSONObject
                                    showToast(error.getString("error"))
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        showToast("Notification sent successfully")
                    } else showToast("Error: " + response.code())
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    showToast("fail" + t.message.toString())
                }

            })
    }

    private fun listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS)
            .document(receiverUser.id)
            .addSnapshotListener { value, error ->
                value.let {
                    value?.getLong(Constants.KEY_USER_AVAILABILITY).let {
                        val availability = value?.getLong(Constants.KEY_USER_AVAILABILITY)?.toInt()
                        isReceiverAvailable = availability == 1
                    }
                    receiverUser.token = value?.getString(Constants.KEY_FCM_TOKEN)
                    if (receiverUser.image == null) {
                        receiverUser.image = value?.getString(Constants.KEY_IMAGE).toString()
                        adapter.receiverProfileImage =
                            getBitmapFromEncodedString(receiverUser.image)!!
                        adapter.notifyItemRangeChanged(0, chatMessages.size)
                    }
                }
                if (isReceiverAvailable) binding.txtAvailability.visibility = View.VISIBLE
                else binding.txtAvailability.visibility = View.GONE

                error.let { return@addSnapshotListener }
            }
    }

    private fun listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener =
        EventListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->

            value.let {
                val count = chatMessages.size
                for (documentChange: DocumentChange in value?.documentChanges!!) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val chatMessage = ChatMessage()
                        chatMessage.senderId =
                            documentChange.document.getString(Constants.KEY_SENDER_ID).toString()
                        chatMessage.receiverId =
                            documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString()
                        chatMessage.message =
                            documentChange.document.getString(Constants.KEY_MESSAGE).toString()
                        chatMessage.dateTime =
                            getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP))
                        chatMessage.dateObject =
                            documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                        chatMessages.add(chatMessage)
                    }
                }
                chatMessages.sortWith(compareBy { it.dateObject })
                if (count == 0) adapter.notifyDataSetChanged()
                else {
                    adapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                    binding.recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)
                }
                binding.recyclerViewChat.visibility = View.VISIBLE
            }

            binding.progressBarChat.visibility = View.GONE
            if (conversationId == null) {
                checkForConversation()
            }
            error.let { return@EventListener }
        }

    private fun getBitmapFromEncodedString(encodedImage: String?): Bitmap? {
        return if (encodedImage != null){
            val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else null
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

    private fun getReadableDateTime(date: Date?): String =
        SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date!!)

    private fun addConversation(conversation: HashMap<String, Any>) {
        database.collection(Constants.KEY_COLLECT_CONVERSATIONS)
            .add(conversation)
            .addOnSuccessListener { conversationId = it.id }
    }

    private fun updateConversation(message: String) {
        val documentReference =
            database.collection(Constants.KEY_COLLECT_CONVERSATIONS).document(conversationId!!)
        documentReference.update(
            Constants.KEY_LAST_MESSAGE,
            message,
            Constants.KEY_TIMESTAMP,
            Date()
        )
    }

    private fun checkForConversation() {
        if (chatMessages.size != 0) {
            checkForConversationRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID),
                receiverUser.id
            )
            checkForConversationRemotely(
                receiverUser.id,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
        }
    }

    private fun checkForConversationRemotely(senderId: String, receiverId: String) {
        database.collection(Constants.KEY_COLLECT_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversationCompleteListener)
    }

    private val conversationCompleteListener = OnCompleteListener<QuerySnapshot> {
        if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
            val documentSnapshot = it.result!!.documents[0]
            conversationId = documentSnapshot.id
        }
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }
}