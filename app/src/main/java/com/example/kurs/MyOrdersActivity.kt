package com.example.tipclik4

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kurs.R
import kotlinx.coroutines.launch

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private val db by lazy { MainDB.getInstance(this) }

    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(application, db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        orderAdapter = OrderAdapter()
        ordersRecyclerView.adapter = orderAdapter
        lifecycleScope.launch {
            loadOrders()
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
                    Toast.makeText(
                        this,
                        "Ошибка загрузки заказов: ${exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    private suspend fun loadOrders() {
        val userId = application.getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("user_id", 0).toLong()
        if(userId != 0L){
            orderViewModel.fetchUserOrders(userId)
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_LONG).show()
        }
    }
}