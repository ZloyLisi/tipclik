package com.example.tipclik4

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class OrderViewModel(application: Application, private val db: MainDB) : AndroidViewModel(application) {
    private val _paperTypes = MutableLiveData<List<PaperType>>()
    val paperTypes: LiveData<List<PaperType>> = _paperTypes
    private val _printTypes = MutableLiveData<List<PrintType>>()
    val printTypes: LiveData<List<PrintType>> = _printTypes
    private val _orderTypes = MutableLiveData<List<OrderType>>()
    val orderTypes: LiveData<List<OrderType>> = _orderTypes

    private val _userOrders = MutableLiveData<Result<List<Order>>>()
    val userOrders: LiveData<Result<List<Order>>> = _userOrders

    private val _saveOrderResult = MutableLiveData<Result<Unit>>()
    val saveOrderResult: LiveData<Result<Unit>> = _saveOrderResult

    private val _allOrders = MutableLiveData<Result<List<Order>>>()
    val allOrders: LiveData<Result<List<Order>>> = _allOrders

    private val _viewOrdersResult = MutableLiveData<Result<List<Order>>>()
    val viewOrdersResult: LiveData<Result<List<Order>>> = _viewOrdersResult


    init {
        viewModelScope.launch {
            fetchPaperTypes()
            fetchPrintTypes()
            fetchOrderTypes()
        }
    }

    private suspend fun fetchPaperTypes() {
        try {
            val paperTypes = withContext(Dispatchers.IO) {
                db.dao().getAllPaperTypes()
            }
            _paperTypes.value = paperTypes
        } catch (e: Exception) {
            Log.d("OrderViewModel", "Ошибка загрузки типов бумаги ${e.message}")
        }
    }

    private suspend fun fetchPrintTypes() {
        try {
            val printTypes = withContext(Dispatchers.IO) {
                db.dao().getAllPrintTypes()
            }
            _printTypes.value = printTypes
        } catch (e: Exception) {
            Log.d("OrderViewModel", "Ошибка загрузки типов печати ${e.message}")
        }
    }

    private suspend fun fetchOrderTypes() {
        try {
            val orderTypes = withContext(Dispatchers.IO) {
                db.dao().getAllOrderTypes()
            }
            _orderTypes.value = orderTypes
        } catch (e: Exception) {
            Log.d("OrderViewModel", "Ошибка загрузки типов заказа ${e.message}")
        }
    }


    suspend fun fetchUserOrders(userId: Long) {
        viewModelScope.launch {
            try {
                val orders = withContext(Dispatchers.IO) {
                    db.dao().getOrdersByUserId(userId)
                }
                _userOrders.value = Result.success(orders)
            } catch (e: Exception) {
                _userOrders.value = Result.failure(e)
                Log.d("OrderViewModel", "Ошибка загрузки заказов ${e.message}")
            }
        }
    }

    suspend fun fetchAllOrders() {
        viewModelScope.launch {
            try {
                val orders = withContext(Dispatchers.IO) {
                    db.dao().getAllOrders()
                }
                _allOrders.value = Result.success(orders)
            } catch (e: Exception) {
                _allOrders.value = Result.failure(e)
                Log.d("OrderViewModel", "Ошибка загрузки всех заказов ${e.message}")
            }
        }
    }


    suspend fun saveOrder(
        userId: Long,
        paperType: String,
        printType: String,
        description: String,
        imageUri: Uri,
        quantity: Int,
        orderType: String
    ) {
        viewModelScope.launch {
            try {
                val paperTypeEntity = db.dao().getPaperTypeByName(paperType)
                val printTypeEntity = db.dao().getPrintTypeByName(printType)
                val orderTypeEntity = db.dao().getOrderTypeByName(orderType)
                if (paperTypeEntity == null || printTypeEntity == null || orderTypeEntity == null) {
                    _saveOrderResult.value = Result.failure(Exception("Не найден тип бумаги/печати/заказа"))
                    return@launch
                }
                val inputStream: InputStream? =
                    getApplication<Application>().contentResolver.openInputStream(imageUri)
                val byteArray = inputStream?.use { it.readBytes() }
                val order = Order(
                    idUser = userId.toInt(),
                    idPaperType = paperTypeEntity.id,
                    idPrintType = printTypeEntity.id,
                    idOrderType = orderTypeEntity.id,
                    description = description,
                    image = byteArray,
                    quantity = quantity
                )
                withContext(Dispatchers.IO) {
                    val existingOrder = order.id?.let { db.dao().getOrderById(it) }
                    if (existingOrder != null) {
                        db.dao().updateOrder(order)
                    } else {
                        db.dao().insertOrder(order)
                    }

                }
                _saveOrderResult.value = Result.success(Unit)

            } catch (e: Exception) {
                _saveOrderResult.value = Result.failure(e)
                Log.d("OrderViewModel", "Ошибка сохранения заказа ${e.message}")
            }
        }
    }
    suspend fun viewOrdersByEmail(email: String) {
        viewModelScope.launch {
            try {
                val orders = withContext(Dispatchers.IO) {
                    val user = db.dao().getUserByEmail(email)
                    if (user != null) {
                        db.dao().getOrdersByUserId(user.id.toLong())
                    } else {
                        emptyList()
                    }
                }
                _viewOrdersResult.postValue(Result.success(orders))
            } catch (e: Exception) {
                _viewOrdersResult.postValue(Result.failure(e))
            }
        }
    }

    class OrderViewModelFactory(private val application: Application, private val db: MainDB) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OrderViewModel(application, db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}