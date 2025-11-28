package com.example.rfidatten.data.repository

import com.example.rfidatten.data.dao.CardDao
import com.example.rfidatten.data.dao.ProfileDao
import com.example.rfidatten.data.entity.Card
import com.example.rfidatten.data.entity.Profile
import kotlinx.coroutines.flow.Flow

class CardRepository(
    private val cardDao: CardDao,
    private val profileDao: ProfileDao
) {
    // Card operations
    fun getAllCards(): Flow<List<Card>> = cardDao.getAllCards()
    
    suspend fun getCardById(cardId: Long): Card? = cardDao.getCardById(cardId)
    
    suspend fun getCardByNumber(cardNumber: String): Card? = cardDao.getCardByNumber(cardNumber)
    
    suspend fun getCardsByProfileId(profileId: Long): List<Card> = 
        cardDao.getCardsByProfileId(profileId)
    
    suspend fun insertCard(card: Card): Long = cardDao.insertCard(card)
    
    suspend fun updateCard(card: Card) = cardDao.updateCard(card)
    
    suspend fun deleteCard(card: Card) = cardDao.deleteCard(card)
    
    suspend fun deleteCardById(cardId: Long) = cardDao.deleteCardById(cardId)
    
    // Profile operations
    fun getAllProfiles(): Flow<List<Profile>> = profileDao.getAllProfiles()
    
    suspend fun getProfileById(profileId: Long): Profile? = profileDao.getProfileById(profileId)
    
    suspend fun insertProfile(profile: Profile): Long = profileDao.insertProfile(profile)
    
    suspend fun updateProfile(profile: Profile) = profileDao.updateProfile(profile)
    
    suspend fun deleteProfile(profile: Profile) = profileDao.deleteProfile(profile)
    
    suspend fun deleteProfileById(profileId: Long) = profileDao.deleteProfileById(profileId)
    
    // Combined operations
    suspend fun createCardWithProfile(cardNumber: String, profile: Profile): Result<Pair<Card, Profile>> {
        return try {
            val profileId = insertProfile(profile)
            val card = Card(cardNumber = cardNumber, profileId = profileId)
            val cardId = insertCard(card)
            Result.success(Pair(card.copy(cardId = cardId), profile.copy(profileId = profileId)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun isCardNumberUnique(cardNumber: String): Boolean {
        return getCardByNumber(cardNumber) == null
    }
}
