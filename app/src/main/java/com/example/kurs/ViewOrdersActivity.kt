package com.example.kurs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tipclik4.MainDB
import com.example.tipclik4.Order
import com.example.tipclik4.OrderViewModel
import com.example.tipclik4.OrderViewModelFactory
import com.example.tipclik4.OrderViewAdapter
import com.example.tipclik4.ManagerEditOrderActivity
import kotlinx.coroutines.launch

class ViewOrdersActivity : AppCompatActivity() {
    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var orderAdapter: OrderViewAdapter
    private lateinit var emailEditText: android.widget.EditText
    private val db by lazy { MainDB.getInstance(this) }
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(application, db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_orders)

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        emailEditText = findViewById(R.id.emailEditText)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)

        orderAdapter = OrderViewAdapter(
            onEditClick = { orderId, _ ->
                startManagerEditOrderActivity(orderId)
            },
            onDeleteClick = { orderId ->
                deleteOrder(orderId)
            },
            onItemClick = { order ->
                startManagerEditOrderActivity(order.id)
            }
        )
        ordersRecyclerView.adapter = orderAdapter

        val searchButton = findViewById<android.widget.Button>(R.id.searchButton)
        searchButton.setOnClickListener {
            searchOrders()
        }
    }

    private fun startManagerEditOrderActivity(orderId: Long) {
        val intent = Intent(this, ManagerEditOrderActivity::class.java)
        intent.putExtra("orderId", orderId)
        startActivity(intent)
    }

    private fun searchOrders() {
        val email = emailEditText.text.toString()
        lifecycleScope.launch {
            orderViewModel.viewOrdersByEmail(email)
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
                    Log.d("ViewOrdersActivity", "Ошибка загрузки данных ${exception?.message}")
                }
                else -> {
                    Log.d("ViewOrdersActivity", "Ошибка загрузки данных")
                }
            }
        }
    }

    private fun deleteOrder(orderId: Long) {
        lifecycleScope.launch {
            db.dao().deleteOrderById(orderId)
            val email = emailEditText.text.toString()
            orderViewModel.viewOrdersByEmail(email)
        }
    }
}