package com.quickrec.storage

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecordingItem(
    val file: File,
    val name: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAt: Date
) {
    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            return if (kb > 1024) {
                String.format(Locale.US, "%.1f MB", kb / 1024.0)
            } else {
                String.format(Locale.US, "%.0f KB", kb)
            }
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            return sdf.format(createdAt)
        }

    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
}
