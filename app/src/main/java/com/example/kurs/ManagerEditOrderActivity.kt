package com.example.tipclik4

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kurs.R
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

class ManagerEditOrderActivity : AppCompatActivity() {

    private lateinit var paperTypeSpinner: Spinner
    private lateinit var printTypeSpinner: Spinner
    private lateinit var orderTypeSpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var imageImageView: ImageView
    private lateinit var saveOrderButton: Button
    private var order: Order? = null
    private var selectedImageUri: Uri? = null
    private val db by lazy { MainDB.getInstance(this) }
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModel.OrderViewModelFactory(application, db)
    }
    private val getImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null && intent.data != null) {
                    selectedImageUri = intent.data!!
                    imageImageView.setImageURI(selectedImageUri)
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_order)

        paperTypeSpinner = findViewById(R.id.paperTypeSpinner)
        printTypeSpinner = findViewById(R.id.printTypeSpinner)
        orderTypeSpinner = findViewById(R.id.orderTypeSpinner)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        quantityEditText = findViewById(R.id.quantityEditText)
        imageImageView = findViewById(R.id.imageImageView)
        saveOrderButton = findViewById(R.id.saveOrderButton)

        val orderId = intent.getLongExtra("orderId", 0)

        lifecycleScope.launch {
            try {
                order = db.dao().getOrderById(orderId)
                order?.let {
                    setOrderData(it)
                    setupSpinners(it)
                } ?: run {
                    Log.e("ManagerEditOrderActivity", "Order is null")
                    Toast.makeText(
                        this@ManagerEditOrderActivity,
                        "Ошибка загрузки заказа",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ManagerEditOrderActivity", "Error loading order data", e)
                Toast.makeText(
                    this@ManagerEditOrderActivity,
                    "Ошибка загрузки заказа",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

        imageImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getImage.launch(intent)
        }

        saveOrderButton.setOnClickListener {
            saveOrder()
        }
    }

    private fun setupSpinners(order: Order) {
        orderViewModel.paperTypes.observe(this) { paperTypes ->
            val paperTypeNames = paperTypes.map { it.name }
            val paperTypeAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, paperTypeNames)
            paperTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            paperTypeSpinner.adapter = paperTypeAdapter
            val paperTypeIndex = paperTypes.indexOfFirst { p -> p.id == order.idPaperType }
            if (paperTypeIndex != -1) {
                paperTypeSpinner.setSelection(paperTypeIndex)
            }
        }

        orderViewModel.printTypes.observe(this) { printTypes ->
            val printTypeNames = printTypes.map { it.name }
            val printTypeAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, printTypeNames)
            printTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            printTypeSpinner.adapter = printTypeAdapter
            val printTypeIndex = printTypes.indexOfFirst { p -> p.id == order.idPrintType }
            if (printTypeIndex != -1) {
                printTypeSpinner.setSelection(printTypeIndex)
            }
        }

        orderViewModel.orderTypes.observe(this) { orderTypes ->
            val orderTypeNames = orderTypes.map { it.name }
            val orderTypeAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, orderTypeNames)
            orderTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            orderTypeSpinner.adapter = orderTypeAdapter
            val orderTypeIndex = orderTypes.indexOfFirst { p -> p.id == order.idOrderType }
            if (orderTypeIndex != -1) {
                orderTypeSpinner.setSelection(orderTypeIndex)
            }
        }
    }

    private fun setOrderData(order: Order) {
        descriptionEditText.setText(order.description)
        quantityEditText.setText(order.quantity.toString())
        order.image?.let {
            val byteArray = it
            val inputStream = ByteArrayInputStream(byteArray)
            val uri = Uri.parse("content://com.example.kurs/image.png")
            imageImageView.setImageURI(uri)
            selectedImageUri = uri
        }
    }


    private fun saveOrder() {
        val userId = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getInt("user_id", 0).toLong()

        val paperType = paperTypeSpinner.selectedItem.toString()
        val printType = printTypeSpinner.selectedItem.toString()
        val orderType = orderTypeSpinner.selectedItem.toString()
        val description = descriptionEditText.text.toString()
        val quantity = quantityEditText.text.toString().toIntOrNull() ?: 0

        lifecycleScope.launch {
            try {
                if (selectedImageUri != null) {
                    orderViewModel.saveOrder(
                        userId,
                        paperType,
                        printType,
                        description,
                        selectedImageUri!!,
                        quantity,
                        orderType
                    )
                    orderViewModel.saveOrderResult.observe(this@ManagerEditOrderActivity) { result ->
                        if (result.isSuccess) {
                            Toast.makeText(
                                this@ManagerEditOrderActivity,
                                "Данные сохранены",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@ManagerEditOrderActivity,
                                "Ошибка сохранения",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@ManagerEditOrderActivity,
                        "Выберите изображение",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("ManagerEditOrderActivity", "Error saving order data", e)
                Toast.makeText(
                    this@ManagerEditOrderActivity,
                    "Ошибка сохранения данных",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}