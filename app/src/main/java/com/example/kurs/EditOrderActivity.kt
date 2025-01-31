package com.example.tipclik4

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kurs.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditOrderActivity : AppCompatActivity() {
    private lateinit var paperTypeSpinner: Spinner
    private lateinit var printTypeSpinner: Spinner
    private lateinit var orderTypeSpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var addImageButton: Button
    private lateinit var orderImage : ImageView
    private var orderId: Long = 0
    private var selectedImageUri: Uri? = null
    private val db by lazy { MainDB.getInstance(this) }
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(application, db)
    }
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(application, db)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EditOrderActivity", "onCreate called")
        setContentView(R.layout.activity_edit_order)

        paperTypeSpinner = findViewById(R.id.paperTypeSpinner)
        printTypeSpinner = findViewById(R.id.printTypeSpinner)
        orderTypeSpinner = findViewById(R.id.orderTypeSpinner)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        quantityEditText = findViewById(R.id.quantityEditText)
        saveButton = findViewById(R.id.saveOrderButton)
        cancelButton = findViewById(R.id.cancelButton)
        addImageButton = findViewById(R.id.addImageButton)
        orderImage = findViewById(R.id.orderImage)

        orderId = intent.getLongExtra("orderId", 0)
        Log.d("EditOrderActivity", "orderId: $orderId")


        lifecycleScope.launch {
            loadSpinners()
            loadData()
        }


        saveButton.setOnClickListener {
            saveOrder()
        }

        cancelButton.setOnClickListener {
            Log.d("EditOrderActivity", "cancelButton clicked")
            finish()
        }
        addImageButton.setOnClickListener{
            openGallery()
        }
    }
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
            val imageUri = it.data?.data
            selectedImageUri = imageUri
            orderImage.setImageURI(imageUri)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("EditOrderActivity", "onStart called")
    }


    override fun onResume() {
        super.onResume()
        Log.d("EditOrderActivity", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("EditOrderActivity", "onPause called")
    }


    override fun onStop() {
        super.onStop()
        Log.d("EditOrderActivity", "onStop called")
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("EditOrderActivity", "onDestroy called")
    }


    private suspend fun loadSpinners() {
        Log.d("EditOrderActivity", "loadSpinners called")
        orderViewModel.paperTypes.observe(this@EditOrderActivity) { paperTypes ->
            Log.d("EditOrderActivity", "paperTypes observed")
            val paperNames = paperTypes.map { it.name }
            val paperAdapter =
                ArrayAdapter(this@EditOrderActivity, android.R.layout.simple_spinner_dropdown_item, paperNames)
            paperTypeSpinner.adapter = paperAdapter
        }

        orderViewModel.printTypes.observe(this@EditOrderActivity) { printTypes ->
            Log.d("EditOrderActivity", "printTypes observed")
            val printNames = printTypes.map { it.name }
            val printAdapter =
                ArrayAdapter(this@EditOrderActivity, android.R.layout.simple_spinner_dropdown_item, printNames)
            printTypeSpinner.adapter = printAdapter
        }
        orderViewModel.orderTypes.observe(this@EditOrderActivity) { orderTypes ->
            Log.d("EditOrderActivity", "orderTypes observed")
            val orderNames = orderTypes.map { it.name }
            val orderAdapter =
                ArrayAdapter(this@EditOrderActivity, android.R.layout.simple_spinner_dropdown_item, orderNames)
            orderTypeSpinner.adapter = orderAdapter
        }
    }

    private suspend fun loadData() {
        Log.d("EditOrderActivity", "loadData called")
        if (orderId == 0L) {
            Log.d("EditOrderActivity", "orderId is 0, returning")
            return
        }
        val order =  withContext(Dispatchers.IO) {
            MainDB.getInstance(this@EditOrderActivity).dao().getOrdersByUserId(
                application.getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getInt("user_id", 0).toLong()
            ).firstOrNull { it.id == orderId }
        }

        if (order == null) {
            Log.d("EditOrderActivity", "Order not found")
            return
        }
        Log.d("EditOrderActivity", "Order loaded: $order")
        val paper = withContext(Dispatchers.IO) {
            MainDB.getInstance(this@EditOrderActivity).dao().getAllPaperTypes().firstOrNull { it.id == order.idPaperType }
        }
        val print = withContext(Dispatchers.IO) {
            MainDB.getInstance(this@EditOrderActivity).dao().getAllPrintTypes().firstOrNull { it.id == order.idPrintType }
        }
        val orderType = withContext(Dispatchers.IO){
            MainDB.getInstance(this@EditOrderActivity).dao().getAllOrderTypes().firstOrNull { it.id == order.idOrderType }
        }
        paperTypeSpinner.setSelection((paperTypeSpinner.adapter as ArrayAdapter<String>).getPosition(paper?.name ?: ""))
        printTypeSpinner.setSelection((printTypeSpinner.adapter as ArrayAdapter<String>).getPosition(print?.name ?: ""))
        orderTypeSpinner.setSelection((orderTypeSpinner.adapter as ArrayAdapter<String>).getPosition(orderType?.name ?: ""))
        descriptionEditText.setText(order.description)
        quantityEditText.setText(order.quantity.toString())
        order.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            orderImage.setImageBitmap(bitmap)
        }
    }

    private fun saveOrder() {
        Log.d("EditOrderActivity", "saveOrder called")
        val selectedPaperType = paperTypeSpinner.selectedItem.toString()
        val selectedPrintType = printTypeSpinner.selectedItem.toString()
        val selectedOrderType = orderTypeSpinner.selectedItem.toString()
        val description = descriptionEditText.text.toString()
        val quantity = quantityEditText.text.toString()
        if (quantity.isBlank()) {
            Log.d("EditOrderActivity", "quantity is blank")
            Toast.makeText(this, "Пожалуйста, заполните количество", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (orderId == 0L) {
                    Log.d("EditOrderActivity", "orderId is 0, cannot save")
                    return@launch
                }
                val userId = application.getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getInt("user_id", 0).toLong()
                Log.d("EditOrderActivity", "userId: $userId")

                val order = withContext(Dispatchers.IO) {
                    MainDB.getInstance(this@EditOrderActivity).dao().getOrdersByUserId(userId)
                        .firstOrNull { it.id == orderId }
                }

                var fileBytes: ByteArray? = null
                if (selectedImageUri != null) {
                    var inputStream: InputStream? = null
                    try {
                        inputStream = contentResolver.openInputStream(selectedImageUri!!)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val resizedBitmap = bitmap?.let { resizeBitmap(it, 200) }
                        fileBytes =  resizedBitmap?.let { bitmapToByteArray(it) }
                    } finally {
                        inputStream?.close()
                    }
                }
                if (order != null) {
                    Log.d("EditOrderActivity", "order found, updating")
                    val updatedOrder = order.copy(
                        idPaperType = withContext(Dispatchers.IO) {
                            MainDB.getInstance(this@EditOrderActivity).dao()
                                .getPaperTypeByName(selectedPaperType)?.id ?: 0
                        },
                        idPrintType = withContext(Dispatchers.IO) {
                            MainDB.getInstance(this@EditOrderActivity).dao()
                                .getPrintTypeByName(selectedPrintType)?.id ?: 0
                        },
                        idOrderType = withContext(Dispatchers.IO) {
                            MainDB.getInstance(this@EditOrderActivity).dao()
                                .getOrderTypeByName(selectedOrderType)?.id ?: 0
                        },
                        description = description,
                        quantity = quantity.toInt(),
                        image = fileBytes ?: order.image
                    )
                    Log.d("EditOrderActivity", "updatedOrder: $updatedOrder")

                    withContext(Dispatchers.IO) {
                        MainDB.getInstance(this@EditOrderActivity).dao().updateOrder(updatedOrder)
                        Log.d("EditOrderActivity", "Order updated successfully")
                    }
                    orderViewModel.fetchUserOrders(userId) // Обновление списка заказов
                    Toast.makeText(this@EditOrderActivity, "Заказ успешно обновлен", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Log.d("EditOrderActivity", "order not found, cannot update")
                    Toast.makeText(this@EditOrderActivity, "Заказ не найден", Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("EditOrderActivity", "Error saving order", e)
                Toast.makeText(this@EditOrderActivity, "Ошибка сохранения заказа: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                finish()
            }
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