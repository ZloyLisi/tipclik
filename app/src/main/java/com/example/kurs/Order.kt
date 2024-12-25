package com.example.tipclik4

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val idUser: Int,
    val idPaperType: Int,
    val idPrintType: Int,
    val idOrderType: Int,
    val description: String?,
    val image: ByteArray?,
    val quantity: Int
)