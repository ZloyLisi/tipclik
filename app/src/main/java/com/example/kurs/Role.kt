package com.example.tipclik4

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Role")
data class Role(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)