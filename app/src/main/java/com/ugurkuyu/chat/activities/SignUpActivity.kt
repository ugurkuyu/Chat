package com.ugurkuyu.chat.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.ugurkuyu.chat.databinding.ActivitySignUpBinding
import com.ugurkuyu.chat.util.Constants
import com.ugurkuyu.chat.util.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var preferenceManager: PreferenceManager
    private var encodedImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    fun setListeners() {
        binding.txtSignIn.setOnClickListener {
            onBackPressed()
        }

        binding.btnSignUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
        binding.txtAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun signUp() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user: HashMap<String, Any> = hashMapOf()
        user[Constants.KEY_NAME] = binding.edtInputName.text.toString()
        user[Constants.KEY_EMAIL] = binding.edtInputEmail.text.toString()
        user[Constants.KEY_PASSWORD] = binding.edtInputPassword.text.toString()
        user[Constants.KEY_IMAGE] = encodedImage!!
        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener {
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, it.id)
                preferenceManager.putString(
                    Constants.KEY_NAME,
                    binding.edtInputName.text.toString()
                )
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage!!)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }.addOnFailureListener {
                loading(false)
                showToast(it.message!!)
            }
    }

    fun encodeImage(bitmap: Bitmap): String? {
        val previewWidth: Int = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap =
            Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    val pickImage: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (it.data != null) {
                    val imageUri: Uri = it.data?.data!!
                    try {
                        val inputStream: InputStream = contentResolver.openInputStream(imageUri)!!
                        val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imgProfile.setImageBitmap(bitmap)
                        binding.txtAddImage.visibility = View.GONE
                        encodedImage = encodeImage(bitmap)!!
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }


    fun isValidSignUpDetails(): Boolean {
        return if (encodedImage == null) {
            showToast("Select Profile Image ")
            false
        } else if (binding.edtInputName.text.toString().trim().isEmpty()) {
            showToast("Enter Name ")
            false
        } else if (binding.edtInputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter Email ")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtInputEmail.text.toString())
                .matches()
        ) {
            showToast("Enter Valid Email Address ")
            false
        } else if (binding.edtInputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter Password ")
            false
        } else if (binding.edtInputConfirmPassword.text.toString().trim().isEmpty()) {
            showToast("Confirm Your Password ")
            false
        } else if (binding.edtInputPassword.text.toString() != binding.edtInputConfirmPassword.text.toString()
        ) {
            showToast("Password doesn't match! ")
            false
        } else
            true
    }

    fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnSignUp.visibility = View.INVISIBLE
            binding.signUpProgressBar.visibility = View.VISIBLE
        } else {
            binding.btnSignUp.visibility = View.VISIBLE
            binding.signUpProgressBar.visibility = View.INVISIBLE
        }
    }
}