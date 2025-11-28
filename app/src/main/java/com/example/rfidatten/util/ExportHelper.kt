package com.example.rfidatten.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.rfidatten.data.dao.AttendanceWithProfile
import com.example.rfidatten.data.entity.Session
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {
    fun exportAttendanceToCSV(
        context: Context,
        session: Session,
        attendanceList: List<AttendanceWithProfile>
    ): Result<File> {
        return try {
            // Create Downloads directory if needed
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsDir.mkdirs()
            
            // Create filename with session name and date
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "Attendance_${session.sessionName.replace(" ", "_")}_$timestamp.csv"
            val file = File(downloadsDir, fileName)
            
            // Write CSV content
            file.bufferedWriter().use { writer ->
                // Header
                writer.write("Session Name,Start Time,End Time,Card Number,Profile Name,Email,Scan Time\n")
                
                // Format timestamps
                val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val sessionStart = dateTimeFormat.format(Date(session.startTime))
                val sessionEnd = dateTimeFormat.format(Date(session.endTime))
                
                // Data rows
                attendanceList.forEach { attendance ->
                    val scanTime = dateTimeFormat.format(Date(attendance.scanTime))
                    writer.write(
                        "${escapeCsv(session.sessionName)}," +
                        "$sessionStart," +
                        "$sessionEnd," +
                        "${escapeCsv(attendance.cardNumber)}," +
                        "${escapeCsv(attendance.profileName)}," +
                        "${escapeCsv(attendance.profileEmail)}," +
                        "$scanTime\n"
                    )
                }
            }
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
    
    fun shareCSVFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share Attendance"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
