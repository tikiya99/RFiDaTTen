package com.example.rfidatten.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rfidatten.data.AppDatabase
import com.example.rfidatten.data.dao.AttendanceWithProfile
import com.example.rfidatten.data.entity.Session
import com.example.rfidatten.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class SessionUiState {
    object Loading : SessionUiState()
    data class Success(
        val currentSessions: List<Session>,
        val pastSessions: List<Session>
    ) : SessionUiState()
    data class Error(val message: String) : SessionUiState()
}

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = SessionRepository(database.sessionDao())
    
    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()
    
    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()
    
    private val _selectedParticipants = MutableStateFlow<Set<Long>>(emptySet())
    val selectedParticipants: StateFlow<Set<Long>> = _selectedParticipants.asStateFlow()
    
    init {
        loadSessions()
    }
    
    private fun loadSessions() {
        viewModelScope.launch {
            try {
                repository.getUpcomingSessions().collect { upcoming ->
                    repository.getCompletedSessions().collect { completed ->
                        _uiState.value = SessionUiState.Success(
                            currentSessions = upcoming,
                            pastSessions = completed
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = SessionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun createSession(
        name: String,
        startTimeMillis: Long,
        endTimeMillis: Long,
        participantCardIds: Set<Long>
    ) {
        viewModelScope.launch {
            try {
                val session = Session(
                    sessionName = name,
                    startTime = startTimeMillis,
                    endTime = endTimeMillis,
                    isActive = false,
                    isCompleted = false
                )
                
                val sessionId = repository.insertSession(session)
                
                // Add participants if any selected
                if (participantCardIds.isNotEmpty()) {
                    repository.addParticipants(sessionId, participantCardIds.toList())
                }
                
                _showCreateDialog.value = false
                _selectedParticipants.value = emptySet()
            } catch (e: Exception) {
                _uiState.value = SessionUiState.Error(e.message ?: "Failed to create session")
            }
        }
    }
    
    fun startSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                repository.startSession(sessionId)
            } catch (e: Exception) {
                _uiState.value = SessionUiState.Error(e.message ?: "Failed to start session")
            }
        }
    }
    
    fun stopSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                repository.stopSession(sessionId)
            } catch (e: Exception) {
                _uiState.value = SessionUiState.Error(e.message ?: "Failed to stop session")
            }
        }
    }
    
    fun deleteSession(session: Session) {
        viewModelScope.launch {
            try {
                repository.deleteSession(session)
            } catch (e: Exception) {
                _uiState.value = SessionUiState.Error(e.message ?: "Failed to delete session")
            }
        }
    }
    
    fun toggleParticipant(cardId: Long) {
        val current = _selectedParticipants.value.toMutableSet()
        if (current.contains(cardId)) {
            current.remove(cardId)
        } else {
            current.add(cardId)
        }
        _selectedParticipants.value = current
    }
    
    fun clearSelectedParticipants() {
        _selectedParticipants.value = emptySet()
    }
    
    fun showCreateDialog() {
        _showCreateDialog.value = true
        _selectedParticipants.value = emptySet()
    }
    
    fun hideCreateDialog() {
        _showCreateDialog.value = false
        _selectedParticipants.value = emptySet()
    }
    
    suspend fun getAttendanceForExport(sessionId: Long): List<AttendanceWithProfile> {
        var result = emptyList<AttendanceWithProfile>()
        repository.getAttendanceWithProfile(sessionId).collect { attendance ->
            result = attendance
        }
        return result
    }
}
