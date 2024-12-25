package com.example.tipclik4

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class OrderViewModel(private val app: Application, private val db: MainDB) : AndroidViewModel(app) {

    private val _saveOrderResult = MutableLiveData<Result<Unit>>()
    val saveOrderResult: LiveData<Result<Unit>> = _saveOrderResult

    private val _paperTypes = MutableLiveData<List<PaperType>>()
    val paperTypes: LiveData<List<PaperType>> = _paperTypes

    private val _printTypes = MutableLiveData<List<PrintType>>()
    val printTypes: LiveData<List<PrintType>> = _printTypes

    private val _orderTypes = MutableLiveData<List<OrderType>>()
    val orderTypes: LiveData<List<OrderType>> = _orderTypes

    private val _userOrders = MutableLiveData<Result<List<Order>>>()
    val userOrders: LiveData<Result<List<Order>>> = _userOrders
    private val _viewOrdersResult = MutableLiveData<Result<List<Order>>>()
    val viewOrdersResult: LiveData<Result<List<Order>>> = _viewOrdersResult


    init {
        loadPaperTypes()
        loadPrintTypes()
        loadOrderTypes()
    }

    private fun loadPaperTypes() {
        viewModelScope.launch {
            val types = withContext(Dispatchers.IO) {
                db.dao().getAllPaperTypes()
            }
            _paperTypes.postValue(types)
        }
    }

    private fun loadPrintTypes() {
        viewModelScope.launch {
            val types = withContext(Dispatchers.IO) {
                db.dao().getAllPrintTypes()
            }
            _printTypes.postValue(types)
        }
    }

    private fun loadOrderTypes() {
        viewModelScope.launch {
            val types = withContext(Dispatchers.IO) {
                db.dao().getAllOrderTypes()
            }
            _orderTypes.postValue(types)
        }
    }



    fun saveOrder(userId: Long, paperType: String, printType: String, description: String, fileUri: Uri, quantity: Int, orderType: String) {
        viewModelScope.launch {
            try {
                val paper = withContext(Dispatchers.IO) { db.dao().getPaperTypeByName(paperType) }
                val print = withContext(Dispatchers.IO) { db.dao().getPrintTypeByName(printType) }
                val order = withContext(Dispatchers.IO) { db.dao().getOrderTypeByName(orderType) }
                var fileBytes: ByteArray? = null
                var inputStream: InputStream? = null
                try {
                    inputStream = app.contentResolver.openInputStream(fileUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val resizedBitmap = bitmap?.let { resizeBitmap(it, 200) }
                    fileBytes =  resizedBitmap?.let { bitmapToByteArray(it) }


                } finally {
                    inputStream?.close()
                }


                withContext(Dispatchers.IO) {
                    val orderEntity = Order(
                        idUser = userId.toInt(),
                        idPaperType = paper?.id ?: 0,
                        idPrintType = print?.id ?: 0,
                        idOrderType = order?.id ?: 0,
                        description = description,
                        image = fileBytes,
                        quantity = quantity
                    )
                    db.dao().insertOrder(orderEntity)
                }
                _saveOrderResult.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _saveOrderResult.postValue(Result.failure(e))
            }
        }
    }


    suspend fun fetchUserOrders(userId: Long) {
        try {
            val orders = withContext(Dispatchers.IO) {
                db.dao().getOrdersByUserId(userId)
            }
            _userOrders.postValue(Result.success(orders))
        } catch (e: Exception) {
            _userOrders.postValue(Result.failure(e))
        }
    }

    suspend fun viewOrdersByEmail(email: String) {
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
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (maxSize / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (maxSize * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
    private fun bitmapToByteArray(bitmap: Bitmap) : ByteArray? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}


