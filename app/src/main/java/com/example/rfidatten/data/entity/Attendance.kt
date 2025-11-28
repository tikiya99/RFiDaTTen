package com.example.rfidatten.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Card::class,
            parentColumns = ["cardId"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"]), Index(value = ["cardId"])]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val attendanceId: Long = 0,
    val sessionId: Long,
    val cardId: Long,
    val scanTime: Long = System.currentTimeMillis(),
    val cardNumber: String // Denormalized for easier export
)

// Junction table for session participants
@Entity(
    tableName = "session_participants",
    primaryKeys = ["sessionId", "cardId"],
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Card::class,
            parentColumns = ["cardId"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"]), Index(value = ["cardId"])]
)
data class SessionParticipant(
    val sessionId: Long,
    val cardId: Long
)
