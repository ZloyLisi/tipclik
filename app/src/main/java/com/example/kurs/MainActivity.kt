package com.example.tipclik4

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kurs.R
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var printFileButton: Button
    private lateinit var printDesignButton: Button
    private lateinit var myOrdersButton: Button
    private lateinit var viewOrdersButton: Button
    private lateinit var profileImageView: ImageView

    private val db by lazy { MainDB.getInstance(this) }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(application, db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printFileButton = findViewById(R.id.printFileButton)
        printDesignButton = findViewById(R.id.printDesignButton)
        myOrdersButton = findViewById(R.id.myOrdersButton)
        viewOrdersButton = findViewById(R.id.viewOrdersButton)
        profileImageView = findViewById(R.id.profileImageView)


        printFileButton.setOnClickListener {
            startActivity(Intent(this, PrintFileActivity::class.java))
        }

        printDesignButton.setOnClickListener {
            startActivity(Intent(this, PrintDesignActivity::class.java))
        }

        myOrdersButton.setOnClickListener {
            startActivity(Intent(this, MyOrdersActivity::class.java))
        }

        viewOrdersButton.setOnClickListener {
            startActivity(Intent(this, ViewOrdersActivity::class.java))
        }

        profileImageView.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        mainViewModel.fetchCurrentUser()

        lifecycleScope.launch {
            loadData()
        }

        mainViewModel.currentUser.observe(this) { result ->
            when {
                result.isSuccess -> {
                    val user = result.getOrNull()
                    if (user != null) {
                        if (user.image != null) {
                            val bitmap = BitmapFactory.decodeByteArray(user.image, 0, user.image.size)
                            profileImageView.setImageBitmap(bitmap)
                        } else {
                            profileImageView.setImageResource(R.drawable.ic_launcher_background)
                        }

                        lifecycleScope.launch {
                            val isManager = MainDB.getInstance(this@MainActivity).dao().getAllRoles()
                                .firstOrNull { it.id == user.idrole }?.name == "Менеджер"

                            if (!isManager) {
                                viewOrdersButton.isEnabled = false
                                viewOrdersButton.alpha = 0.5f
                            }
                        }

                    } else {
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
    }
    private suspend fun loadData(){
    }
}