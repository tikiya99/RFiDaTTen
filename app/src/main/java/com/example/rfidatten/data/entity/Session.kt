package com.example.rfidatten.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    indices = [Index(value = ["sessionId"], unique = true)]
)
data class Session(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0,
    val sessionName: String,
    val startTime: Long, // Timestamp in milliseconds
    val endTime: Long, // Timestamp in milliseconds
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
