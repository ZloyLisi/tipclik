package com.example.kurs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tipclik4.EditOrderActivity
import com.example.tipclik4.MainDB
import com.example.tipclik4.OrderViewAdapter
import com.example.tipclik4.OrderViewModel
import com.example.tipclik4.OrderViewModelFactory
import kotlinx.coroutines.launch

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var orderAdapter: OrderViewAdapter
    private val db by lazy { MainDB.getInstance(this) }
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(application, db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)

        orderAdapter = OrderViewAdapter(
            onEditClick = { orderId, isManager ->
                startEditOrderActivity(orderId, isManager)
            },
            onDeleteClick = { orderId ->
                deleteOrder(orderId)
            },
            onItemClick = { order ->
                startEditOrderActivity(order.id, false)
            }
        )
        ordersRecyclerView.adapter = orderAdapter

        val userId = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getInt("user_id", 0).toLong()
        lifecycleScope.launch {
            orderViewModel.fetchUserOrders(userId)
        }
        orderViewModel.userOrders.observe(this) { result ->
            when {
                result.isSuccess -> {
                    val orders = result.getOrNull()
                    if (orders != null) {
                        orderAdapter.submitList(orders)
                    }
                }
                result.isFailure -> {
                    val exception = result.exceptionOrNull()
                    Log.d("MyOrdersActivity", "Ошибка загрузки данных ${exception?.message}")
                }
                else -> {
                    Log.d("MyOrdersActivity", "Ошибка загрузки данных")
                }
            }
        }
    }

    private fun startEditOrderActivity(orderId: Long, isManager: Boolean) {
        val intent = Intent(this, EditOrderActivity::class.java)
        intent.putExtra("orderId", orderId)
        intent.putExtra("isManager", isManager)
        startActivity(intent)
    }
    private fun deleteOrder(orderId: Long) {
        lifecycleScope.launch {
            db.dao().deleteOrderById(orderId)
            val userId = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getInt("user_id", 0).toLong()
            orderViewModel.fetchUserOrders(userId)
        }
    }
}