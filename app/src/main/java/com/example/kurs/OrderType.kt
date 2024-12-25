package com.example.tipclik4

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OrderType (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)