package com.ugurkuyu.chat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ugurkuyu.chat.databinding.ItemContainerRecentConversationsBinding
import com.ugurkuyu.chat.listeners.ConversationListener
import com.ugurkuyu.chat.models.ChatMessage
import com.ugurkuyu.chat.models.User

class RecentConversationsAdapter(
    val chatMessage: List<ChatMessage>,
    val listener: ConversationListener
) :
    RecyclerView.Adapter<RecentConversationsAdapter.RecentConversationsViewHolder>() {

    inner class RecentConversationsViewHolder(private val binding: ItemContainerRecentConversationsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(message: ChatMessage) {
            binding.imageProfile.setImageBitmap(getConversationImage(message.conversationImage))
            binding.textName.text = message.conversationName
            binding.textRecentMessage.text = message.message
            binding.root.setOnClickListener {
                val user = User()
                user.id = message.conversationId
                user.name = message.conversationName
                user.image = message.conversationImage
                listener.onConversationClickedListener(user)
            }
        }
    }

    private fun getConversationImage(encodedImage: String): Bitmap {
        val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentConversationsViewHolder {
        return RecentConversationsViewHolder(
            ItemContainerRecentConversationsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecentConversationsViewHolder, position: Int) {
        holder.setData(chatMessage[position])
    }

    override fun getItemCount(): Int = chatMessage.size
}