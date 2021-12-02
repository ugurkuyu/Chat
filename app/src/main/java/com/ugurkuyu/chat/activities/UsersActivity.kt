package com.ugurkuyu.chat.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.ugurkuyu.chat.adapters.UsersAdapter
import com.ugurkuyu.chat.databinding.ActivityUsersBinding
import com.ugurkuyu.chat.listeners.UserListener
import com.ugurkuyu.chat.models.User
import com.ugurkuyu.chat.util.Constants
import com.ugurkuyu.chat.util.PreferenceManager

class UsersActivity : AppCompatActivity(), UserListener {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    fun setListeners() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    fun getUsers() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener {
            loading(false)
            val currentUserId: String = preferenceManager.getString(Constants.KEY_USER_ID)
            if (it.isSuccessful && it.result != null) {
                val users: ArrayList<User> = arrayListOf()
                for (queryDocumentSnapShot: QueryDocumentSnapshot in it.result!!) {
                    if (currentUserId == queryDocumentSnapShot.id) continue

                    val user = User()
                    user.name = queryDocumentSnapShot.getString(Constants.KEY_NAME)!!
                    user.email = queryDocumentSnapShot.getString(Constants.KEY_EMAIL)!!
                    user.image = queryDocumentSnapShot.getString(Constants.KEY_IMAGE)!!
                    user.token = queryDocumentSnapShot.getString(Constants.KEY_FCM_TOKEN)
                    user.id = queryDocumentSnapShot.id

                    users.add(user)
                }
                if (users.size > 0) {
                    val usersAdapter: UsersAdapter = UsersAdapter(users, this)
                    binding.recyclerView.adapter = usersAdapter
                    binding.recyclerView.visibility = View.VISIBLE
                } else showErrorMessage()

            } else showErrorMessage()
        }
    }

    fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.userProgressBar.visibility = View.VISIBLE
        } else {
            binding.userProgressBar.visibility = View.INVISIBLE
        }
    }

    fun showErrorMessage() {
        binding.txtErrorMessage.text = String.format("%s", "No user available")
        binding.txtErrorMessage.visibility = View.VISIBLE
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }
}