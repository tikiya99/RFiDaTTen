package com.example.rfidatten.data.repository

import com.example.rfidatten.data.dao.SessionDao
import com.example.rfidatten.data.entity.Attendance
import com.example.rfidatten.data.entity.Session
import com.example.rfidatten.data.entity.SessionParticipant
import com.example.rfidatten.data.dao.AttendanceWithProfile
import com.example.rfidatten.data.dao.SessionWithCount
import kotlinx.coroutines.flow.Flow

class SessionRepository(
    private val sessionDao: SessionDao
) {
    // Session operations
    fun getAllSessions(): Flow<List<Session>> = sessionDao.getAllSessions()
    
    fun getActiveSessions(): Flow<List<Session>> = sessionDao.getActiveSessions()
    
    fun getCompletedSessions(): Flow<List<Session>> = sessionDao.getCompletedSessions()
    
    fun getUpcomingSessions(): Flow<List<Session>> = sessionDao.getUpcomingSessions()
    
    suspend fun getSessionById(sessionId: Long): Session? = sessionDao.getSessionById(sessionId)
    
    fun getSessionWithCount(sessionId: Long): Flow<SessionWithCount?> = 
        sessionDao.getSessionWithCount(sessionId)
    
    suspend fun insertSession(session: Session): Long = sessionDao.insertSession(session)
    
    suspend fun updateSession(session: Session) = sessionDao.updateSession(session)
    
    suspend fun deleteSession(session: Session) = sessionDao.deleteSession(session)
    
    suspend fun deleteSessionById(sessionId: Long) = sessionDao.deleteSessionById(sessionId)
    
    suspend fun startSession(sessionId: Long) {
        val session = getSessionById(sessionId)
        session?.let {
            updateSession(it.copy(isActive = true))
        }
    }
    
    suspend fun stopSession(sessionId: Long) {
        val session = getSessionById(sessionId)
        session?.let {
            updateSession(it.copy(isActive = false, isCompleted = true))
        }
    }
    
    // Attendance operations
    suspend fun recordAttendance(attendance: Attendance): Long = 
        sessionDao.insertAttendance(attendance)
    
    fun getAttendanceBySession(sessionId: Long): Flow<List<Attendance>> = 
        sessionDao.getAttendanceBySession(sessionId)
    
    fun getAttendanceWithProfile(sessionId: Long): Flow<List<AttendanceWithProfile>> = 
        sessionDao.getAttendanceWithProfile(sessionId)
    
    fun getAttendanceCount(sessionId: Long): Flow<Int> = 
        sessionDao.getAttendanceCount(sessionId)
    
    suspend fun hasAttendance(sessionId: Long, cardId: Long): Boolean = 
        sessionDao.hasAttendance(sessionId, cardId)
    
    // Session participant operations
    suspend fun addParticipant(sessionId: Long, cardId: Long) {
        sessionDao.insertParticipant(SessionParticipant(sessionId, cardId))
    }
    
    suspend fun addParticipants(sessionId: Long, cardIds: List<Long>) {
        val participants = cardIds.map { SessionParticipant(sessionId, it) }
        sessionDao.insertParticipants(participants)
    }
    
    suspend fun clearParticipants(sessionId: Long) {
        sessionDao.deleteParticipantsBySession(sessionId)
    }
    
    suspend fun getParticipantsBySession(sessionId: Long): List<SessionParticipant> = 
        sessionDao.getParticipantsBySession(sessionId)
    
    suspend fun isParticipant(sessionId: Long, cardId: Long): Boolean = 
        sessionDao.isParticipant(sessionId, cardId)
    
    suspend fun getParticipantCount(sessionId: Long): Int = 
        sessionDao.getParticipantCount(sessionId)
    
    // Helper function to check if session allows all cards or only selected participants
    suspend fun canScanCard(sessionId: Long, cardId: Long): Boolean {
        val participantCount = getParticipantCount(sessionId)
        return if (participantCount == 0) {
            // No specific participants selected, allow all cards
            true
        } else {
            // Check if card is in participant list
            isParticipant(sessionId, cardId)
        }
    }
}
