package com.example.tipclik4

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AuthViewModel(application: Application, private val db: MainDB) : AndroidViewModel(application) {

    private val appDao = db.dao()
    private val sharedPreferences = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _authResult = MutableLiveData<Result<User?>>()
    val authResult: LiveData<Result<User?>> = _authResult

    private val _registrationResult = MutableLiveData<Result<User?>>()
    val registrationResult: LiveData<Result<User?>> = _registrationResult

    fun registerUser(firstName: String, lastName: String, middleName: String?, email: String, password: String, image: ByteArray?, idrole: Long) {
        viewModelScope.launch {
            _registrationResult.value = try {
                val passwordHash = hashPassword(password)
                val user = User(firstName = firstName, lastName = lastName, middleName = middleName, email = email, passwordHash = passwordHash, image = image, idrole = idrole)
                withContext(Dispatchers.IO) {
                    appDao.insertUser(user)
                    val createdUser = appDao.getUserByEmail(email)
                    if(createdUser != null){
                        sharedPreferences.edit().putInt("user_id", createdUser.id.toInt()).apply()
                        Result.success(createdUser)
                    }
                    else {
                        throw Exception("User was not created")
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = try {
                withContext(Dispatchers.IO){
                    val passwordHash = hashPassword(password)
                    val user = appDao.getUserByEmail(email)
                    if(user != null && user.passwordHash == passwordHash){
                        sharedPreferences.edit().putInt("user_id", user.id.toInt()).apply()
                        Result.success(user)
                    } else {
                        throw Exception("Invalid username or password")
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }


    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }


}