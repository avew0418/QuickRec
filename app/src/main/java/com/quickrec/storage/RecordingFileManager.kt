package com.quickrec.storage

import android.content.ContentValues
import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingFileManager(private val context: Context) {

    companion object {
        private const val TAG = "RecordingFileManager"
        private const val APP_DIR_NAME = "QuickRec"
        private const val FILE_PREFIX = "REC_"
        private const val FILE_EXTENSION = ".m4a"
        private val DATE_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        
        // Manual string to avoid API 31 requirement for Environment.DIRECTORY_RECORDINGS
        private const val DIRECTORY_RECORDINGS = "Recordings"
    }

    /**
     * Target: /storage/emulated/0/Recordings/QuickRec
     */
    fun createNewRecordingFile(): File {
        val timestamp = DATE_FORMAT.format(Date())
        val fileName = "$FILE_PREFIX$timestamp$FILE_EXTENSION"
        
        val publicDir = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_RECORDINGS), APP_DIR_NAME)
        if (!publicDir.exists()) {
            publicDir.mkdirs()
        }
        
        return File(publicDir, fileName)
    }

    /**
     * After recording is done, notify MediaStore so it shows up in "My Files".
     */
    fun scanFile(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                put(MediaStore.Audio.Media.RELATIVE_PATH, "$DIRECTORY_RECORDINGS/$APP_DIR_NAME")
                put(MediaStore.Audio.Media.IS_PENDING, 0)
            }
            try {
                context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                Log.d(TAG, "File scanned via MediaStore: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "MediaStore insertion failed", e)
            }
        } else {
            // Pre-Android 10 approach (though Galaxy A16 is Android 14)
            Log.d(TAG, "Legacy scan for: ${file.absolutePath}")
        }
    }

    fun listRecordings(): List<RecordingItem> {
        val publicDir = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_RECORDINGS), APP_DIR_NAME)
        return publicDir.listFiles()
            ?.filter { it.isFile && it.extension == "m4a" }
            ?.sortedByDescending { it.lastModified() }
            ?.mapNotNull { fileToRecordingItem(it) }
            ?: emptyList()
    }

    fun deleteRecording(item: RecordingItem): Boolean {
        return try {
            val deleted = item.file.delete()
            if (deleted) {
                Log.i(TAG, "Deleted: ${item.name}")
                context.contentResolver.delete(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    "${MediaStore.Audio.Media.DISPLAY_NAME} = ?",
                    arrayOf(item.name)
                )
            }
            deleted
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
        if (file.length() == 0L) return 0L
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
