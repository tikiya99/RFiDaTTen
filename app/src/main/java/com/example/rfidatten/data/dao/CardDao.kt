package com.example.rfidatten.data.dao

import androidx.room.*
import com.example.rfidatten.data.entity.Card
import com.example.rfidatten.data.entity.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY cardId DESC")
    fun getAllCards(): Flow<List<Card>>
    
    @Query("SELECT * FROM cards WHERE cardId = :cardId")
    suspend fun getCardById(cardId: Long): Card?
    
    @Query("SELECT * FROM cards WHERE cardNumber = :cardNumber")
    suspend fun getCardByNumber(cardNumber: String): Card?
    
    @Query("SELECT * FROM cards WHERE profileId = :profileId")
    suspend fun getCardsByProfileId(profileId: Long): List<Card>
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCard(card: Card): Long
    
    @Update
    suspend fun updateCard(card: Card)
    
    @Delete
    suspend fun deleteCard(card: Card)
    
    @Query("DELETE FROM cards WHERE cardId = :cardId")
    suspend fun deleteCardById(cardId: Long)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY profileId DESC")
    fun getAllProfiles(): Flow<List<Profile>>
    
    @Query("SELECT * FROM profiles WHERE profileId = :profileId")
    suspend fun getProfileById(profileId: Long): Profile?
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProfile(profile: Profile): Long
    
    @Update
    suspend fun updateProfile(profile: Profile)
    
    @Delete
    suspend fun deleteProfile(profile: Profile)
    
    @Query("DELETE FROM profiles WHERE profileId = :profileId")
    suspend fun deleteProfileById(profileId: Long)
}
