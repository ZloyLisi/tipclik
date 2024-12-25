package com.example.tipclik4

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "middle_name") val middleName: String? = null,
    val email: String,
    val passwordHash: String,
    val image: ByteArray? = null,
    @ColumnInfo(name = "idrole") val idrole: Long
)