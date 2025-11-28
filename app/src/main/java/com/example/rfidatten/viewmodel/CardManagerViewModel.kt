package com.example.rfidatten.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rfidatten.data.AppDatabase
import com.example.rfidatten.data.entity.Card
import com.example.rfidatten.data.entity.Profile
import com.example.rfidatten.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CardWithProfile(
    val card: Card,
    val profile: Profile
)

sealed class CardManagerUiState {
    object Loading : CardManagerUiState()
    data class Success(val cardsWithProfiles: List<CardWithProfile>) : CardManagerUiState()
    data class Error(val message: String) : CardManagerUiState()
}

class CardManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = CardRepository(database.cardDao(), database.profileDao())
    
    private val _uiState = MutableStateFlow<CardManagerUiState>(CardManagerUiState.Loading)
    val uiState: StateFlow<CardManagerUiState> = _uiState.asStateFlow()
    
    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()
    
    init {
        loadCards()
    }
    
    private fun loadCards() {
        viewModelScope.launch {
            try {
                repository.getAllCards().collect { cards ->
                    val cardsWithProfiles = cards.mapNotNull { card ->
                        val profile = repository.getProfileById(card.profileId)
                        profile?.let { CardWithProfile(card, it) }
                    }
                    _uiState.value = CardManagerUiState.Success(cardsWithProfiles)
                }
            } catch (e: Exception) {
                _uiState.value = CardManagerUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun createCard(cardNumber: String, name: String, age: Int, birthday: String, email: String) {
        viewModelScope.launch {
            try {
                if (!repository.isCardNumberUnique(cardNumber)) {
                    _uiState.value = CardManagerUiState.Error("Card number already exists")
                    return@launch
                }
                
                val profile = Profile(
                    name = name,
                    age = age,
                    birthday = birthday,
                    email = email
                )
                
                repository.createCardWithProfile(cardNumber, profile)
                _showCreateDialog.value = false
            } catch (e: Exception) {
                _uiState.value = CardManagerUiState.Error(e.message ?: "Failed to create card")
            }
        }
    }
    
    fun updateCard(card: Card) {
        viewModelScope.launch {
            try {
                repository.updateCard(card)
            } catch (e: Exception) {
                _uiState.value = CardManagerUiState.Error(e.message ?: "Failed to update card")
            }
        }
    }
    
    fun deleteCard(card: Card) {
        viewModelScope.launch {
            try {
                repository.deleteCard(card)
            } catch (e: Exception) {
                _uiState.value = CardManagerUiState.Error(e.message ?: "Failed to delete card")
            }
        }
    }
    
    fun showCreateDialog() {
        _showCreateDialog.value = true
    }
    
    fun hideCreateDialog() {
        _showCreateDialog.value = false
    }
}
