package com.ugurkuyu.chat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ugurkuyu.chat.databinding.ActivitySignInBinding
import com.ugurkuyu.chat.util.Constants
import com.ugurkuyu.chat.util.PreferenceManager

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(applicationContext)
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    fun setListeners() {
        binding.txtCreateNewAccount.setOnClickListener {
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }

        binding.btnSignIn.setOnClickListener {
            if (isValidSignInDetails()) signIn()
        }
    }

    fun signIn() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding.edtInputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding.edtInputPassword.text.toString())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                    val documentSnapShot: DocumentSnapshot = it.result!!.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapShot.id)
                    preferenceManager.putString(
                        Constants.KEY_NAME,
                        documentSnapShot.getString(Constants.KEY_NAME)!!
                    )
                    preferenceManager.putString(
                        Constants.KEY_IMAGE,
                        documentSnapShot.getString(Constants.KEY_IMAGE)!!
                    )
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    loading(false)
                    showToast("Unable to sign in")
                }
            }
    }

    fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun isValidSignInDetails(): Boolean {
        return if (binding.edtInputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter Email")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtInputEmail.text.toString())
                .matches()
        ) {
            showToast("Enter valid email address")
            false
        } else if (binding.edtInputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            false
        } else true
    }

    fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnSignIn.visibility = View.INVISIBLE
            binding.signInProgressBar.visibility = View.VISIBLE
        } else {
            binding.btnSignIn.visibility = View.VISIBLE
            binding.signInProgressBar.visibility = View.INVISIBLE
        }
    }
}