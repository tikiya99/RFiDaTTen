package com.example.rfidatten.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profiles",
    indices = [Index(value = ["profileId"], unique = true)]
)
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val profileId: Long = 0,
    val name: String,
    val age: Int,
    val birthday: String, // Format: YYYY-MM-DD
    val email: String
)

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["profileId"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardNumber"], unique = true), Index(value = ["profileId"])]
)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val cardId: Long = 0,
    val cardNumber: String, // RFID card unique number
    val profileId: Long
)
