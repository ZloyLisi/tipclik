package com.example.tipclik4

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kurs.R
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


class RegistrationActivity : AppCompatActivity() {
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var imageView: ImageView

    private var selectedImageUri: Uri? = null

    private val db by lazy { MainDB.getInstance(this) }


    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(application, db)
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let{
                val bitmap =  uriToBitmap(it)
                imageView.setImageBitmap(bitmap)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        middleNameEditText = findViewById(R.id.middleNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        roleSpinner = findViewById(R.id.roleSpinner)
        registerButton = findViewById(R.id.registerButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        imageView = findViewById(R.id.imageView)


        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            filePickerLauncher.launch(intent)
        }


        registerButton.setOnClickListener {
            registerUser()
        }

        lifecycleScope.launch {
            loadSpinners()
        }

        authViewModel.registrationResult.observe(this) { result ->
            when {
                result.isSuccess -> {
                    val user = result.getOrNull()
                    if (user != null) {
                        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putInt("user_id", user.id.toInt())
                        editor.putString("user_email", user.email)
                        editor.apply()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
                result.isFailure -> {
                    val exception = result.exceptionOrNull()
                    Toast.makeText(this, "Ошибка регистрации: ${exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private suspend fun loadSpinners() {
        val roles =  MainDB.getInstance(this).dao().getAllRoles()
        val roleNames = roles.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roleNames)
        roleSpinner.adapter = adapter
    }

    private fun registerUser() {
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val middleName = middleNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val selectedRole = roleSpinner.selectedItem.toString()

        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || selectedRole.isBlank()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_LONG).show()
            return
        }
        lifecycleScope.launch {
            val roles = MainDB.getInstance(this@RegistrationActivity).dao().getAllRoles()
            val role = roles.firstOrNull{it.name == selectedRole}
            if(role == null){
                Toast.makeText(this@RegistrationActivity, "Роль не найдена", Toast.LENGTH_LONG).show()
                return@launch
            }
            val resizedBitmap = selectedImageUri?.let { uriToBitmap(it) }?.let { resizeBitmap(it, 200) }
            val byteArray = resizedBitmap?.let { bitmapToByteArray(it) }

            authViewModel.registerUser(firstName, lastName, if(middleName.isBlank()) null else middleName, email, password, byteArray, role.id )
        }
    }
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if(bitmapRatio > 1){
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
    private fun uriToBitmap(uri: Uri): Bitmap? {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
    private fun bitmapToByteArray(bitmap: Bitmap) : ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
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