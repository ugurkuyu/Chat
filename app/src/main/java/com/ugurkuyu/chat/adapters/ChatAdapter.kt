package com.ugurkuyu.chat.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ugurkuyu.chat.databinding.ItemContainerReceivedMessageBinding
import com.ugurkuyu.chat.databinding.ItemContainerSentMessageBinding
import com.ugurkuyu.chat.models.ChatMessage

class ChatAdapter(
    var receiverProfileImage: Bitmap?,
    private var chatMessages: List<ChatMessage>,
    private var senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {

        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    inner class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            binding.txtMessage.text = chatMessage.message
            binding.txtDateTime.text = chatMessage.dateTime
        }

    }

    inner class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap?) {
            binding.txtMessage.text = chatMessage.message
            binding.txtDateTime.text = chatMessage.dateTime
            receiverProfileImage.let {
                binding.imgProfile.setImageBitmap(receiverProfileImage)
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT)
            SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        else ReceivedMessageViewHolder(
            ItemContainerReceivedMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT)
            (holder as SentMessageViewHolder).setData(chatMessages[position])
        else (holder as ReceivedMessageViewHolder).setData(
            chatMessages[position],
            receiverProfileImage
        )
    }

    override fun getItemCount(): Int = chatMessages.size

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId == senderId) VIEW_TYPE_SENT
        else VIEW_TYPE_RECEIVED
    }


}