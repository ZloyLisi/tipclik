package com.example.tipclik4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurs.R
import kotlinx.coroutines.launch

class  ViewOrdersActivity: AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var viewOrdersButton: Button
    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter

    private val db by lazy { MainDB.getInstance(this) }
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(application, db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_orders)

        emailEditText = findViewById(R.id.emailEditText)
        viewOrdersButton = findViewById(R.id.viewOrdersButton)
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)

        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        orderAdapter = OrderAdapter()
        ordersRecyclerView.adapter = orderAdapter

        viewOrdersButton.setOnClickListener {
            viewOrders()
        }

        orderViewModel.viewOrdersResult.observe(this) { result ->
            when {
                result.isSuccess -> {
                    val orders = result.getOrNull()
                    if (orders != null) {
                        orderAdapter.submitList(orders)
                    }
                }
                result.isFailure -> {
                    val exception = result.exceptionOrNull()
                    Toast.makeText(
                        this,
                        "Ошибка загрузки заказов: ${exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun viewOrders() {
        val email = emailEditText.text.toString()
        if (email.isBlank()) {
            Toast.makeText(this, "Пожалуйста, введите email пользователя", Toast.LENGTH_LONG)
                .show()
            return
        }
        lifecycleScope.launch {
            orderViewModel.viewOrdersByEmail(email)
        }
    }
}