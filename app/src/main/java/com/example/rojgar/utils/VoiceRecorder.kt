package com.example.rojgar.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class VoiceRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false

    fun startRecording(onError: (String) -> Unit): File? {
        try {
            // Create output file
            val fileName = "voice_${System.currentTimeMillis()}.m4a"
            outputFile = File(context.cacheDir, fileName)

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile?.absolutePath)

                try {
                    prepare()
                    start()
                    isRecording = true
                } catch (e: IOException) {
                    onError("Failed to start recording: ${e.message}")
                    return null
                }
            }

            return outputFile
        } catch (e: Exception) {
            onError("Failed to initialize recorder: ${e.message}")
            return null
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return null

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            // Check if file exists and is not empty
            outputFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    return file
                } else {
                    file.delete()
                    return null
                }
            }
            return null
        } catch (e: Exception) {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            outputFile?.delete()
            return null
        }
    }

    fun cancelRecording() {
        if (isRecording) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                // Ignore errors during cancel
            }
            mediaRecorder = null
            isRecording = false
            outputFile?.delete()
            outputFile = null
        }
    }

    fun isCurrentlyRecording() = isRecording

    fun release() {
        cancelRecording()
    }
}