package com.example.rojgar.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Alternative VoiceRecorder with better compatibility
 * Use this if 3GP quality is not satisfactory
 */
class VoiceRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false

    companion object {
        // Use AAC in MP4 container for better compatibility
        private const val OUTPUT_FORMAT = MediaRecorder.OutputFormat.MPEG_4
        private const val AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC
        private const val AUDIO_SAMPLING_RATE = 44100 // 44.1kHz CD quality
        private const val AUDIO_BITRATE = 128000 // 128 kbps
        private const val AUDIO_CHANNELS = 1 // Mono for voice
    }

    fun startRecording(onError: (String) -> Unit): File? {
        try {
            val fileName = "voice_${System.currentTimeMillis()}.m4a"
            outputFile = File(context.cacheDir, fileName)

            Log.d("VoiceRecorder", "=== Starting Recording ===")
            Log.d("VoiceRecorder", "Output file: ${outputFile?.absolutePath}")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                // Set audio source
                setAudioSource(MediaRecorder.AudioSource.MIC)

                // FIXED: Use MP4/AAC for universal compatibility
                setOutputFormat(OUTPUT_FORMAT)
                setOutputFile(outputFile?.absolutePath)
                setAudioEncoder(AUDIO_ENCODER)

                // Set audio quality parameters
                setAudioSamplingRate(AUDIO_SAMPLING_RATE)
                setAudioEncodingBitRate(AUDIO_BITRATE)
                setAudioChannels(AUDIO_CHANNELS)

                try {
                    prepare()
                    Log.d("VoiceRecorder", "MediaRecorder prepared")

                    // Log recording parameters
                    Log.d("VoiceRecorder", "Recording parameters:")
                    Log.d("VoiceRecorder", "  Format: MPEG-4 (.m4a)")
                    Log.d("VoiceRecorder", "  Encoder: AAC")
                    Log.d("VoiceRecorder", "  Sample Rate: ${AUDIO_SAMPLING_RATE}Hz")
                    Log.d("VoiceRecorder", "  Bitrate: ${AUDIO_BITRATE / 1000}kbps")
                    Log.d("VoiceRecorder", "  Channels: $AUDIO_CHANNELS")

                    start()
                    isRecording = true

                    Log.d("VoiceRecorder", "✅ Recording started successfully")
                    Log.d("VoiceRecorder", "  File: ${outputFile?.name}")

                } catch (e: IOException) {
                    Log.e("VoiceRecorder", "❌ Failed to start recording", e)
                    onError("Failed to start recording: ${e.message}")
                    release()
                    return null
                } catch (e: IllegalStateException) {
                    Log.e("VoiceRecorder", "❌ IllegalStateException during recording", e)
                    onError("Recording state error: ${e.message}")
                    release()
                    return null
                } catch (e: RuntimeException) {
                    Log.e("VoiceRecorder", "❌ RuntimeException during recording", e)
                    onError("Recording error: ${e.message}")
                    release()
                    return null
                }
            }

            return outputFile

        } catch (e: Exception) {
            Log.e("VoiceRecorder", "❌ Failed to initialize recorder", e)
            onError("Failed to initialize recorder: ${e.message}")
            release()
            return null
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) {
            Log.w("VoiceRecorder", "Stop called but not recording")
            return null
        }

        try {
            Log.d("VoiceRecorder", "Stopping recording...")

            mediaRecorder?.apply {
                try {
                    stop()
                    Log.d("VoiceRecorder", "MediaRecorder stopped")
                } catch (e: RuntimeException) {
                    Log.e("VoiceRecorder", "Error stopping recorder", e)
                    // Continue to release resources
                }
                release()
                Log.d("VoiceRecorder", "MediaRecorder released")
            }

            mediaRecorder = null
            isRecording = false

            // Validate the recorded file
            outputFile?.let { file ->
                val fileExists = file.exists()
                val fileSize = if (fileExists) file.length() else 0

                Log.d("VoiceRecorder", "Recording file validation:")
                Log.d("VoiceRecorder", "  Path: ${file.absolutePath}")
                Log.d("VoiceRecorder", "  Exists: $fileExists")
                Log.d("VoiceRecorder", "  Size: $fileSize bytes (${fileSize / 1024}KB)")
                Log.d("VoiceRecorder", "  Extension: ${file.extension}")

                // File must exist and be at least 1KB
                if (fileExists && fileSize > 1024) {
                    Log.d("VoiceRecorder", "✓ Recording saved successfully")
                    return file
                } else {
                    Log.e("VoiceRecorder", "✗ Recording file is invalid or too small")
                    if (fileExists) file.delete()
                    return null
                }
            }

            Log.e("VoiceRecorder", "Output file is null")
            return null

        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Exception during stop recording", e)

            try {
                mediaRecorder?.release()
            } catch (releaseException: Exception) {
                Log.e("VoiceRecorder", "Error releasing recorder", releaseException)
            }

            mediaRecorder = null
            isRecording = false
            outputFile?.delete()
            return null
        }
    }

    fun cancelRecording() {
        Log.d("VoiceRecorder", "Cancelling recording...")

        if (isRecording) {
            try {
                mediaRecorder?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.e("VoiceRecorder", "Error stopping during cancel", e)
                    }
                    release()
                }
            } catch (e: Exception) {
                Log.e("VoiceRecorder", "Error during cancel", e)
            }

            mediaRecorder = null
            isRecording = false

            outputFile?.let { file ->
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d("VoiceRecorder", "Temp file deleted: $deleted")
                }
            }
            outputFile = null
        }
    }

    fun isCurrentlyRecording() = isRecording

    fun release() {
        Log.d("VoiceRecorder", "Releasing VoiceRecorder")
        cancelRecording()
    }
}