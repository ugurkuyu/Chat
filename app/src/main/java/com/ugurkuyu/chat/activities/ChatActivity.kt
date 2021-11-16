package com.ugurkuyu.chat.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.ugurkuyu.chat.R
import com.ugurkuyu.chat.databinding.ActivityChatBinding
import com.ugurkuyu.chat.models.User
import com.ugurkuyu.chat.util.Constants

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User
        binding.txtName.text = receiverUser.name
    }

    private fun setListeners() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }
}