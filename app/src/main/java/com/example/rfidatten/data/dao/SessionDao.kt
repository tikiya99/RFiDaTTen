package com.example.rfidatten.data.dao

import androidx.room.*
import com.example.rfidatten.data.entity.Attendance
import com.example.rfidatten.data.entity.Session
import com.example.rfidatten.data.entity.SessionParticipant
import kotlinx.coroutines.flow.Flow

// Data class for session with attendance count
data class SessionWithCount(
    val sessionId: Long,
    val sessionName: String,
    val startTime: Long,
    val endTime: Long,
    val isActive: Boolean,
    val isCompleted: Boolean,
    val createdAt: Long,
    val attendanceCount: Int
)

// Data class for attendance with profile details
data class AttendanceWithProfile(
    val attendanceId: Long,
    val sessionId: Long,
    val cardId: Long,
    val cardNumber: String,
    val scanTime: Long,
    val profileName: String,
    val profileEmail: String
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<Session>>
    
    @Query("SELECT * FROM sessions WHERE isActive = 1")
    fun getActiveSessions(): Flow<List<Session>>
    
    @Query("SELECT * FROM sessions WHERE isCompleted = 1 ORDER BY endTime DESC")
    fun getCompletedSessions(): Flow<List<Session>>
    
    @Query("SELECT * FROM sessions WHERE isCompleted = 0 ORDER BY startTime ASC")
    fun getUpcomingSessions(): Flow<List<Session>>
    
    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: Long): Session?
    
    @Query("""
        SELECT s.*, COUNT(a.attendanceId) as attendanceCount 
        FROM sessions s 
        LEFT JOIN attendance a ON s.sessionId = a.sessionId 
        WHERE s.sessionId = :sessionId 
        GROUP BY s.sessionId
    """)
    fun getSessionWithCount(sessionId: Long): Flow<SessionWithCount?>
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: Session): Long
    
    @Update
    suspend fun updateSession(session: Session)
    
    @Delete
    suspend fun deleteSession(session: Session)
    
    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)
    
    // Attendance operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long
    
    @Query("SELECT * FROM attendance WHERE sessionId = :sessionId ORDER BY scanTime DESC")
    fun getAttendanceBySession(sessionId: Long): Flow<List<Attendance>>
    
    @Query("""
        SELECT a.*, p.name as profileName, p.email as profileEmail 
        FROM attendance a 
        INNER JOIN cards c ON a.cardId = c.cardId 
        INNER JOIN profiles p ON c.profileId = p.profileId 
        WHERE a.sessionId = :sessionId 
        ORDER BY a.scanTime DESC
    """)
    fun getAttendanceWithProfile(sessionId: Long): Flow<List<AttendanceWithProfile>>
    
    @Query("SELECT COUNT(*) FROM attendance WHERE sessionId = :sessionId")
    fun getAttendanceCount(sessionId: Long): Flow<Int>
    
    @Query("""
        SELECT COUNT(*) > 0 FROM attendance 
        WHERE sessionId = :sessionId AND cardId = :cardId
    """)
    suspend fun hasAttendance(sessionId: Long, cardId: Long): Boolean
    
    // Session participant operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParticipant(participant: SessionParticipant)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParticipants(participants: List<SessionParticipant>)
    
    @Query("DELETE FROM session_participants WHERE sessionId = :sessionId")
    suspend fun deleteParticipantsBySession(sessionId: Long)
    
    @Query("SELECT * FROM session_participants WHERE sessionId = :sessionId")
    suspend fun getParticipantsBySession(sessionId: Long): List<SessionParticipant>
    
    @Query("""
        SELECT COUNT(*) > 0 FROM session_participants 
        WHERE sessionId = :sessionId AND cardId = :cardId
    """)
    suspend fun isParticipant(sessionId: Long, cardId: Long): Boolean
    
    @Query("SELECT COUNT(*) FROM session_participants WHERE sessionId = :sessionId")
    suspend fun getParticipantCount(sessionId: Long): Int
}
