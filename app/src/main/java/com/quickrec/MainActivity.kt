package com.quickrec

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickrec.hotkey.VolumeKeyInterceptor
import com.quickrec.hotkey.VolumeKeyStateMachine
import com.quickrec.permission.PermissionHelper
import com.quickrec.recording.AudioRecorderManager
import com.quickrec.recording.RecordingState
import com.quickrec.storage.RecordingFileManager
import com.quickrec.ui.theme.QuickRecTheme

class MainActivity : ComponentActivity() {

    private lateinit var recorderManager: AudioRecorderManager
    private lateinit var hotkeyInterceptor: VolumeKeyInterceptor

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, getString(R.string.permission_rationale), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileManager = RecordingFileManager(this)
        recorderManager = AudioRecorderManager(this, fileManager)

        val stateMachine = VolumeKeyStateMachine(
            onAudioHotkey = {
                Log.i("MainActivity", "Audio hotkey triggered")
                toggleRecording()
            },
            onVideoHotkey = {
                Log.i("MainActivity", "Video hotkey triggered but not implemented")
                runOnUiThread {
                    Toast.makeText(this, R.string.video_not_implemented, Toast.LENGTH_SHORT).show()
                }
            }
        )
        hotkeyInterceptor = VolumeKeyInterceptor(stateMachine)

        setContent {
            QuickRecTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    MainScreen(recorderManager)
                }
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (!PermissionHelper.hasRecordAudioPermission(this)) {
            requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun toggleRecording() {
        val currentState = recorderManager.state.value
        Log.d("MainActivity", "toggleRecording: current state is ${currentState::class.simpleName}")
        if (currentState is RecordingState.Recording) {
            recorderManager.stopRecording()
            runOnUiThread {
                Toast.makeText(this, R.string.recording_saved, Toast.LENGTH_SHORT).show()
            }
        } else {
            recorderManager.startRecording()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (hotkeyInterceptor.dispatchKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        recorderManager.release()
    }
}

@Composable
fun MainScreen(recorderManager: AudioRecorderManager) {
    val recordingState by recorderManager.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val statusText = when (recordingState) {
            is RecordingState.Idle -> stringResource(R.string.status_idle)
            is RecordingState.Recording -> stringResource(R.string.status_recording)
            is RecordingState.Error -> (recordingState as RecordingState.Error).message
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.headlineLarge,
            color = if (recordingState is RecordingState.Recording) Color.Red else Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (recordingState is RecordingState.Recording) {
                    recorderManager.stopRecording()
                } else {
                    recorderManager.startRecording()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (recordingState is RecordingState.Recording) Color.DarkGray else Color.Red
            )
        ) {
            Text(
                if (recordingState is RecordingState.Recording) "STOP" else "REC",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = stringResource(R.string.hotkey_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
