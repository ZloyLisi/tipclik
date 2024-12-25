package com.example.tipclik4

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kurs.R
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var changeImageButton: Button

    private val db by lazy { MainDB.getInstance(this) }

    private val mainViewModel: MainViewModel by viewModels{
        MainViewModelFactory(application, db)
    }
    private var selectedImageUri: Uri? = null
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let{
                val bitmap =  uriToBitmap(it)
                profileImageView.setImageBitmap(bitmap)
                lifecycleScope.launch {
                    updateUserImage()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        profileImageView = findViewById(R.id.profileImageView)
        nameTextView = findViewById(R.id.nameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        logoutButton = findViewById(R.id.logoutButton)
        changeImageButton = findViewById(R.id.changePhotoButton)
        changeImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            filePickerLauncher.launch(intent)
        }


        logoutButton.setOnClickListener {
            logout()
        }

        mainViewModel.currentUser.observe(this) { result ->
            when {
                result.isSuccess -> {
                    val user = result.getOrNull()
                    if (user != null) {
                        if(user.image != null){
                            val bitmap = BitmapFactory.decodeByteArray(user.image, 0, user.image.size)
                            profileImageView.setImageBitmap(bitmap)
                        } else{
                            profileImageView.setImageResource(R.drawable.ic_launcher_foreground)
                        }
                        nameTextView.text = user.firstName + " " + user.lastName + " " + (user.middleName ?: "")
                        emailTextView.text = user.email

                    } else{
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        Toast.makeText(this, "Необходимо авторизоваться", Toast.LENGTH_LONG).show()
                    }
                }
                result.isFailure -> {
                    Toast.makeText(this, "Ошибка получения пользователя", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
        lifecycleScope.launch {
            mainViewModel.fetchCurrentUser()
        }
    }
    private  fun logout(){
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("user_id", 0)
        editor.putString("user_email", "")
        editor.apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    private suspend fun updateUserImage(){
        val userId = application.getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("user_id", 0).toLong()
        if(userId != 0L){
            val user =  MainDB.getInstance(this@SettingsActivity).dao().getUserByEmail(
                application.getSharedPreferences("app_prefs", MODE_PRIVATE).getString("user_email", "") ?: ""
            )
            if(user != null){
                val updatedUser = user.copy(image = selectedImageUri?.let { uriToByteArray(it) })
                MainDB.getInstance(this@SettingsActivity).dao().insertUser(updatedUser)
            }
        }
        else {
            Toast.makeText(this, "Необходимо авторизоваться", Toast.LENGTH_LONG).show()
        }


    }
    private fun uriToBitmap(uri: Uri): android.graphics.Bitmap? {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun uriToByteArray(uri: Uri) : ByteArray? {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            return  inputStream?.use {
                it.readBytes()
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
        return null
    }
}