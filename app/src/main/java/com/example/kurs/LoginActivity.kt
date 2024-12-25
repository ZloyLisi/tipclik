package com.example.tipclik4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kurs.R
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(){

private lateinit var passwordEditText: EditText
private lateinit var loginButton: Button
private lateinit var registerTextView: TextView
private lateinit var emailEditText: EditText
private val db by lazy { MainDB.getInstance(this) }

private val authViewModel: AuthViewModel by viewModels {
    AuthViewModelFactory(application, db)
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)


    emailEditText = findViewById(R.id.emailEditText)
    passwordEditText = findViewById(R.id.passwordEditText)
    loginButton = findViewById(R.id.loginButton)
    registerTextView = findViewById(R.id.registerTextView)

    loginButton.setOnClickListener {
        loginUser()
    }

    registerTextView.setOnClickListener {
        startActivity(Intent(this, RegistrationActivity::class.java))
    }


    authViewModel.authResult.observe(this) { result ->
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
                Toast.makeText(this, "Ошибка авторизации: ${exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

private fun loginUser() {
    val email = emailEditText.text.toString()
    val password = passwordEditText.text.toString()

    if (email.isBlank() || password.isBlank()) {
        Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_LONG).show()
        return
    }
    lifecycleScope.launch {
        authViewModel.loginUser(email, password)
    }
}
}