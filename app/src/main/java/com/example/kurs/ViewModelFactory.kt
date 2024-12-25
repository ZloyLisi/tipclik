package com.example.tipclik4

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AuthViewModelFactory(private val application: Application, private val db: MainDB) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(application, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainViewModelFactory(private val application: Application, private val db: MainDB) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application, db.dao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class OrderViewModelFactory(private val application: Application, private val db: MainDB) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            return OrderViewModel(application, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}