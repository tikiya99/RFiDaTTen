package com.example.rfidatten.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rfidatten.data.dao.CardDao
import com.example.rfidatten.data.dao.ProfileDao
import com.example.rfidatten.data.dao.SessionDao
import com.example.rfidatten.data.entity.Attendance
import com.example.rfidatten.data.entity.Card
import com.example.rfidatten.data.entity.Profile
import com.example.rfidatten.data.entity.Session
import com.example.rfidatten.data.entity.SessionParticipant

@Database(
    entities = [
        Profile::class,
        Card::class,
        Session::class,
        Attendance::class,
        SessionParticipant::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun profileDao(): ProfileDao
    abstract fun sessionDao(): SessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rfid_attendance_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
