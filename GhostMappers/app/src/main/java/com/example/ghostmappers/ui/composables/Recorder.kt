package com.example.ghostmappers.ui.composables

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ghostmappers.R
import com.example.ghostmappers.services.RecorderPermissionManager
import com.example.ghostmappers.ui.theme.Beige
import com.example.ghostmappers.ui.theme.LightBlue
import com.example.ghostmappers.ui.theme.Maron
import com.example.ghostmappers.ui.theme.Orange
import java.io.File

@Composable
fun VoiceRecorder(
    audioFileUri: Uri?,
    onAudioFileUriChange: (Uri) -> Unit,
    onRecordingDeleted: () -> Unit
) {
    val context = LocalContext.current
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }
    var showExistingAudio by remember { mutableStateOf(true) }

    if (audioFileUri != null && showExistingAudio) {
        ExistingAudio(audioFileUri)
    } else {
        if (showPermissionDeniedMessage) {
            CharacteristicAction(
                label = "Sound",
                buttonText = "Record",
                enabled = false,
                onClick = {
                    Toast.makeText(
                        context,
                        "Recording permission is required to record.",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                icon = R.drawable.microphone
            )
        } else {
            RecorderPermissionManager(
                onPermissionDenied = { showPermissionDeniedMessage = true },
                content = {
                    VoiceRecorderContent(
                        audioFileUri, onAudioFileUriChange, onRecordingDeleted,
                        onDone = {
                            showExistingAudio = false
                        })
                }
            )
        }
    }
}

@Composable
private fun VoiceRecorderContent(
    audioFileUri: Uri?,
    onAudioFileUriChange: (Uri) -> Unit,
    onRecordingDeleted: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val localAudioFile = remember { File(context.cacheDir, "recorded_audio.3gp") }

    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var recordingExists by remember(audioFileUri) { mutableStateOf(audioFileUri != null || localAudioFile.exists()) }

    DisposableEffect(Unit) {
        onDispose {
            recorder?.release()
            player?.release()

            if (localAudioFile.exists()) {
                localAudioFile.delete()
            }

            if (recordingExists) {
                onRecordingDeleted()
            }
        }


    }


    fun startRecording() {
        try {
            if (localAudioFile.exists()) localAudioFile.delete()
            recorder = (
                    MediaRecorder(context)
                    ).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setOutputFile(localAudioFile.absolutePath)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    prepare()
                    start()
                }
            isRecording = true
            Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to start recording: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        isRecording = false
        recordingExists = true
        Toast.makeText(context, "Recording stopped", Toast.LENGTH_SHORT).show()

        val authority = "${context.packageName}.provider"
        val fileUri = FileProvider.getUriForFile(context, authority, localAudioFile)
        onDone()
        onAudioFileUriChange(fileUri)

    }

    fun playRecording() {
        try {
            player = MediaPlayer().apply {
                val uriToPlay = audioFileUri ?: FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    localAudioFile
                )
                setDataSource(context, uriToPlay)
                prepare()
                start()
                setOnCompletionListener {
                    isPlaying = false
                    it.release() // Release player when done
                }
            }
            isPlaying = true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not play recording: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    fun stopPlaying() {
        player?.apply {
            stop()
            release()
        }
        player = null
        isPlaying = false
    }

    fun deleteRecording() {
        if (isPlaying) stopPlaying()
        if (localAudioFile.exists()) localAudioFile.delete()
        recordingExists = false
        Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
        onRecordingDeleted()

    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Beige)
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(color = if (isRecording) Orange else Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Sound Recording",
                modifier = Modifier.size(24.dp),
                tint = if (isRecording) Color.White else LightBlue,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("Sound", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))


        if (isRecording) {
            // Show Stop button while recording
            Button(
                onClick = { stopRecording() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop Recording")
            }
        } else {
            if (recordingExists) {
                // Show Play/Stop and Delete buttons if recording exists
                OutlinedButton(
                    onClick = { if (isPlaying) stopPlaying() else playRecording() },
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, LightBlue)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Play/Stop",
                        tint = LightBlue
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { deleteRecording() },
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Maron)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Recording",
                        tint = Color.White
                    )
                }

            } else {
                // Show Record button initially
                Button(
                    onClick = { startRecording() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Maron,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(90.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("Record")
                }
            }
        }
    }
}

@Composable
private fun ExistingAudio(audioFileUri: Uri?) {
    val context = LocalContext.current

    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }


    fun playRecording() {
        try {
            player = MediaPlayer().apply {
                setDataSource(context, audioFileUri!!)
                prepare()
                start()
                setOnCompletionListener {
                    isPlaying = false
                    it.release() // Release player when done
                }
            }
            isPlaying = true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not play recording: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    fun stopPlaying() {
        player?.apply {
            stop()
            release()
        }
        player = null
        isPlaying = false
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Beige)
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(color = if (isRecording) Orange else Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Sound Recording",
                modifier = Modifier.size(24.dp),
                tint = if (isRecording) Color.White else LightBlue,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("Sound", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))

        // Show Play/Stop and Delete buttons if recording exists
        OutlinedButton(
            onClick = { if (isPlaying) stopPlaying() else playRecording() },
            shape = CircleShape,
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(1.dp, LightBlue)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Play/Stop",
                tint = LightBlue
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

    }
}