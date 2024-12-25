package com.example.tipclik4

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kurs.R
import kotlinx.coroutines.launch

class  PrintDesignActivity : AppCompatActivity() {
    private lateinit var paperTypeSpinner: Spinner
    private lateinit var printTypeSpinner: Spinner
    private lateinit var orderTypeSpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var fileButton: Button
    private lateinit var quantityEditText: EditText
    private lateinit var saveOrderButton: Button
    private var selectedFileUri: Uri? = null
    private val db by lazy { MainDB.getInstance(this) }
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(application, db)
    }
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedFileUri = result.data?.data
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_design)

        paperTypeSpinner = findViewById(R.id.paperTypeSpinner)
        printTypeSpinner = findViewById(R.id.printTypeSpinner)
        orderTypeSpinner = findViewById(R.id.orderTypeSpinner)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        fileButton = findViewById(R.id.fileButton)
        quantityEditText = findViewById(R.id.quantityEditText)
        saveOrderButton = findViewById(R.id.SaveButton)

        fileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            filePickerLauncher.launch(intent)
        }


        saveOrderButton.setOnClickListener {
            saveOrder()
        }

        lifecycleScope.launch {
            loadSpinners()
        }

        orderViewModel.saveOrderResult.observe(this) { result ->
            when {
                result.isSuccess -> {
                    Toast.makeText(this, "Заказ успешно сохранён", Toast.LENGTH_LONG).show()
                    finish()
                }
                result.isFailure -> {
                    val exception = result.exceptionOrNull()
                    Toast.makeText(this, "Ошибка сохранения заказа: ${exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private suspend fun loadSpinners(){
        orderViewModel.paperTypes.observe(this) { paperTypes ->
            val paperNames = paperTypes.map { it.name }
            val paperAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paperNames)
            paperTypeSpinner.adapter = paperAdapter
        }

        orderViewModel.printTypes.observe(this) { printTypes ->
            val printNames = printTypes.map { it.name }
            val printAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, printNames)
            printTypeSpinner.adapter = printAdapter
        }
        orderViewModel.orderTypes.observe(this) { orderTypes ->
            val orderNames = orderTypes.map { it.name }
            val orderAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, orderNames)
            orderTypeSpinner.adapter = orderAdapter
        }
    }
    private fun saveOrder() {
        val selectedPaperType = paperTypeSpinner.selectedItem.toString()
        val selectedPrintType = printTypeSpinner.selectedItem.toString()
        val selectedOrderType = orderTypeSpinner.selectedItem.toString()
        val description = descriptionEditText.text.toString()
        val quantity = quantityEditText.text.toString()
        val userId =
            application.getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("user_id", 0).toLong()

        if (userId == 0L) {
            Toast.makeText(this, "Необходимо авторизоваться", Toast.LENGTH_LONG).show()
            return
        }
        if (selectedFileUri == null || quantity.isBlank()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            orderViewModel.saveOrder(userId, selectedPaperType, selectedPrintType, description, selectedFileUri!!, quantity.toInt(), selectedOrderType)
        }
    }
}