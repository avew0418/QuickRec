package com.quickrec

import android.app.Application
import com.quickrec.recording.AudioRecorderManager
import com.quickrec.storage.RecordingFileManager

class QuickRecApp : Application() {

    lateinit var fileManager: RecordingFileManager
        private set
    
    lateinit var recorderManager: AudioRecorderManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        fileManager = RecordingFileManager(this)
        recorderManager = AudioRecorderManager(this, fileManager)
    }

    companion object {
        lateinit var instance: QuickRecApp
            private set
    }
}
