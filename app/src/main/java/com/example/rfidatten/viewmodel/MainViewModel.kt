package com.example.rfidatten.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rfidatten.data.AppDatabase
import com.example.rfidatten.data.entity.Attendance
import com.example.rfidatten.data.entity.Session
import com.example.rfidatten.data.repository.CardRepository
import com.example.rfidatten.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScanResult {
    data class Success(val cardNumber: String, val profileName: String) : ScanResult()
    data class Failure(val reason: String) : ScanResult()
    object Idle : ScanResult()
}

sealed class MainUiState {
    object NoActiveSession : MainUiState()
    data class ActiveSession(
        val session: Session,
        val attendanceCount: Int
    ) : MainUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val sessionRepository = SessionRepository(database.sessionDao())
    private val cardRepository = CardRepository(database.cardDao(), database.profileDao())
    
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.NoActiveSession)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _scanResult = MutableStateFlow<ScanResult>(ScanResult.Idle)
    val scanResult: StateFlow<ScanResult> = _scanResult.asStateFlow()
    
    private val _activeSessionId = MutableStateFlow<Long?>(null)
    
    init {
        loadActiveSession()
    }
    
    private fun loadActiveSession() {
        viewModelScope.launch {
            try {
                sessionRepository.getActiveSessions().collect { sessions ->
                    val activeSession = sessions.firstOrNull()
                    if (activeSession != null) {
                        _activeSessionId.value = activeSession.sessionId
                        sessionRepository.getAttendanceCount(activeSession.sessionId).collect { count ->
                            _uiState.value = MainUiState.ActiveSession(activeSession, count)
                        }
                    } else {
                        _activeSessionId.value = null
                        _uiState.value = MainUiState.NoActiveSession
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.NoActiveSession
            }
        }
    }
    
    fun simulateScan(cardNumber: String) {
        viewModelScope.launch {
            try {
                val sessionId = _activeSessionId.value
                if (sessionId == null) {
                    _scanResult.value = ScanResult.Failure("No active session")
                    return@launch
                }
                
                // Check if card exists
                val card = cardRepository.getCardByNumber(cardNumber)
                if (card == null) {
                    _scanResult.value = ScanResult.Failure("Card not registered")
                    return@launch
                }
                
                // Check if card can scan in this session (participant check)
                if (!sessionRepository.canScanCard(sessionId, card.cardId)) {
                    _scanResult.value = ScanResult.Failure("Card not authorized for this session")
                    return@launch
                }
                
                // Check if already scanned
                if (sessionRepository.hasAttendance(sessionId, card.cardId)) {
                    _scanResult.value = ScanResult.Failure("Already scanned in this session")
                    return@launch
                }
                
                // Get profile name
                val profile = cardRepository.getProfileById(card.profileId)
                if (profile == null) {
                    _scanResult.value = ScanResult.Failure("Profile not found")
                    return@launch
                }
                
                // Record attendance
                val attendance = Attendance(
                    sessionId = sessionId,
                    cardId = card.cardId,
                    cardNumber = cardNumber,
                    scanTime = System.currentTimeMillis()
                )
                
                sessionRepository.recordAttendance(attendance)
                _scanResult.value = ScanResult.Success(cardNumber, profile.name)
                
            } catch (e: Exception) {
                _scanResult.value = ScanResult.Failure(e.message ?: "Scan failed")
            }
        }
    }
    
    fun clearScanResult() {
        _scanResult.value = ScanResult.Idle
    }
}
