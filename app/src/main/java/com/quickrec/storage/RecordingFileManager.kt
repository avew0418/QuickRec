package com.quickrec.storage

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingFileManager(private val context: Context) {

    companion object {
        private const val TAG = "RecordingFileManager"
        private const val RECORDINGS_DIR = "recordings"
        private const val FILE_PREFIX = "REC_"
        private const val FILE_EXTENSION = ".m4a"
        private val DATE_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    }

    private val recordingsDir: File
        get() {
            val dir = File(context.filesDir, RECORDINGS_DIR)
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun createNewRecordingFile(): File {
        val timestamp = DATE_FORMAT.format(Date())
        return File(recordingsDir, "$FILE_PREFIX$timestamp$FILE_EXTENSION")
    }

    fun listRecordings(): List<RecordingItem> {
        return recordingsDir.listFiles()
            ?.filter { it.isFile && it.extension == "m4a" }
            ?.sortedByDescending { it.lastModified() }
            ?.mapNotNull { fileToRecordingItem(it) }
            ?: emptyList()
    }

    fun deleteRecording(item: RecordingItem): Boolean {
        return try {
            item.file.delete().also {
                if (it) Log.i(TAG, "Deleted: ${item.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed: ${item.name}", e)
            false
        }
    }

    private fun fileToRecordingItem(file: File): RecordingItem? {
        return try {
            val duration = getAudioDuration(file)
            val date = extractDateFromFileName(file.name) ?: Date(file.lastModified())
            RecordingItem(
                file = file,
                name = file.name,
                displayName = file.nameWithoutExtension
                    .removePrefix(FILE_PREFIX)
                    .replace("_", " "),
                durationMs = duration,
                sizeBytes = file.length(),
                createdAt = date
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read: ${file.name}", e)
            null
        }
    }

    private fun getAudioDuration(file: File): Long {
        return try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(file.absolutePath)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun extractDateFromFileName(name: String): Date? {
        return try {
            val dateStr = name.removePrefix(FILE_PREFIX).removeSuffix(FILE_EXTENSION)
            DATE_FORMAT.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}
