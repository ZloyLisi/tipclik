package com.example.tipclik4

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application, private val appDao: AppDao) :
    AndroidViewModel(application) {

    private val sharedPreferences =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableLiveData<Result<User?>>()
    val currentUser: LiveData<Result<User?>> = _currentUser



    fun fetchCurrentUser() {
        Log.d("MainViewModel", "fetchCurrentUser called")
        viewModelScope.launch {
            _currentUser.value = withContext(Dispatchers.IO) {
                try {
                    val userId = sharedPreferences.getInt("user_id", 0)
                    Log.d("MainViewModel", "userId: $userId")
                    if (userId != 0) {
                        val user = appDao.getUserByEmail(
                            sharedPreferences.getString("user_email", "") ?: ""
                        )
                        Log.d("MainViewModel", "user found: $user")
                        Result.success(user)
                    } else {
                        Log.d("MainViewModel", "user id is 0")
                        Result.success(null)
                    }
                }
                catch (e: Exception){
                    Log.e("MainViewModel", "Error fetching user", e)
                    Result.failure(e)
                }
            }
        }
    }
}