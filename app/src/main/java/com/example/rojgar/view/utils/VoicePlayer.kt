package com.example.rojgar.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import android.media.AudioManager
import java.io.IOException

// Update the VoicePlayer class
class VoicePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaybackActive = false
    private var currentAudioUrl: String? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playAudio(
        audioUrl: String,
        onCompletion: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            Log.d("VoicePlayer", "=== Starting Audio Playback ===")
            Log.d("VoicePlayer", "Audio URL: $audioUrl")

            // CRITICAL: Validate URL format FIRST
            if (audioUrl.isBlank()) {
                Log.e("VoicePlayer", "❌ URL is blank")
                onError("Audio URL is empty")
                return
            }

            // Clean URL
            val cleanUrl = audioUrl.trim()

            // If already playing, stop it first
            if (isPlaybackActive && currentAudioUrl == cleanUrl) {
                Log.d("VoicePlayer", "Already playing this audio, stopping...")
                stopAudio()
                onCompletion()
                return
            }
            else if (isPlaybackActive) {
                Log.d("VoicePlayer", "Stopping current audio before playing new one")
                stopAudio()
            }

            currentAudioUrl = cleanUrl

            // CRITICAL FIX: Handle URL properly
            val finalUrl = if (cleanUrl.contains("cloudinary.com") && cleanUrl.startsWith("http://")) {
                cleanUrl.replace("http://", "https://")
            } else {
                cleanUrl
            }

            Log.d("VoicePlayer", "Final URL: $finalUrl")

            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                // Set audio attributes for voice messages
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                isLooping = false

                // Set listeners
                setOnErrorListener { mp, what, extra ->
                    Log.e("VoicePlayer", "❌ MediaPlayer error occurred")
                    Log.e("VoicePlayer", "  What: $what (${getErrorType(what)})")
                    Log.e("VoicePlayer", "  Extra: $extra (${getExtraError(extra)})")

                    val errorMsg = when (extra) {
                        MediaPlayer.MEDIA_ERROR_IO -> "Network error while loading audio"
                        MediaPlayer.MEDIA_ERROR_MALFORMED -> "Audio file is corrupted"
                        MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "Audio format not supported"
                        MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "Connection timed out"
                        else -> "Playback error: $what/$extra"
                    }

                    isPlaybackActive = false
                    currentAudioUrl = null
                    onError(errorMsg)

                    try {
                        reset()
                        release()
                    } catch (e: Exception) {
                        Log.e("VoicePlayer", "Error during cleanup", e)
                    }
                    mediaPlayer = null

                    true
                }

                setOnCompletionListener {
                    Log.d("VoicePlayer", "✅ Playback completed successfully")
                    isPlaybackActive = false
                    currentAudioUrl = null
                    onCompletion()
                }

                setOnPreparedListener { mp ->
                    try {
                        val duration = mp.duration
                        Log.d("VoicePlayer", "✅ MediaPlayer prepared successfully")
                        Log.d("VoicePlayer", "  Duration: ${duration}ms (${duration / 1000}s)")

                        // Set volume to maximum
                        mp.setVolume(1.0f, 1.0f)

                        // Start playback
                        mp.start()
                        isPlaybackActive = true

                        Log.d("VoicePlayer", "✅ Playback started successfully")
                        Log.d("VoicePlayer", "  Is playing: ${mp.isPlaying}")

                    } catch (e: Exception) {
                        Log.e("VoicePlayer", "❌ Error starting playback", e)
                        onError("Failed to start playback: ${e.message}")
                        isPlaybackActive = false
                        currentAudioUrl = null
                    }
                }

                try {
                    Log.d("VoicePlayer", "Setting data source...")

                    // CRITICAL FIX: Use setDataSource(String) for HTTP URLs
                    // NOT setDataSource(Context, Uri) which expects ContentResolver URIs
                    setDataSource(finalUrl)
                    Log.d("VoicePlayer", "✅ Data source set successfully")

                    // Prepare asynchronously
                    Log.d("VoicePlayer", "Starting async preparation...")
                    prepareAsync()

                } catch (e: IOException) {
                    Log.e("VoicePlayer", "❌ IO Error setting data source", e)
                    onError("Cannot access audio file: ${e.message}")
                    isPlaybackActive = false
                    currentAudioUrl = null
                } catch (e: IllegalArgumentException) {
                    Log.e("VoicePlayer", "❌ Invalid URL format", e)
                    onError("Invalid audio URL: ${e.message}")
                    isPlaybackActive = false
                    currentAudioUrl = null
                } catch (e: SecurityException) {
                    Log.e("VoicePlayer", "❌ Security exception", e)
                    onError("Permission denied: ${e.message}")
                    isPlaybackActive = false
                    currentAudioUrl = null
                } catch (e: Exception) {
                    Log.e("VoicePlayer", "❌ Error setting data source", e)
                    onError("Failed to load audio: ${e.message}")
                    isPlaybackActive = false
                    currentAudioUrl = null
                }
            }

        } catch (e: Exception) {
            Log.e("VoicePlayer", "❌ Failed to initialize player", e)
            onError("Failed to initialize player: ${e.message}")
            isPlaybackActive = false
            currentAudioUrl = null

            try {
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (releaseException: Exception) {
                Log.e("VoicePlayer", "Error releasing player", releaseException)
            }
        }
    }
    private fun cleanupMediaPlayer() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Error releasing player", e)
        } finally {
            mediaPlayer = null
        }
    }

    private fun getErrorType(what: Int): String {
        return when (what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> "UNKNOWN"
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "SERVER_DIED"
            else -> "ERROR_$what"
        }
    }

    private fun getExtraError(extra: Int): String {
        return when (extra) {
            MediaPlayer.MEDIA_ERROR_IO -> "IO_ERROR"
            MediaPlayer.MEDIA_ERROR_MALFORMED -> "MALFORMED"
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "UNSUPPORTED"
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "TIMED_OUT"
            -2147483648 -> "LOW_LEVEL_ERROR"
            else -> "ERROR_$extra"
        }
    }

    fun stopAudio() {
        try {
            Log.d("VoicePlayer", "Stopping audio playback")

            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    Log.d("VoicePlayer", "MediaPlayer stopped")
                }
                reset()
                release()
                Log.d("VoicePlayer", "MediaPlayer released")
            }
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Error stopping audio", e)
        } finally {
            mediaPlayer = null
            isPlaybackActive = false
            currentAudioUrl = null
        }
    }

    fun pauseAudio() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    isPlaybackActive = false
                    Log.d("VoicePlayer", "Playback paused")
                }
            }
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Error pausing audio", e)
        }
    }

    fun resumeAudio() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    isPlaybackActive = true
                    Log.d("VoicePlayer", "Playback resumed")
                }
            }
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Error resuming audio", e)
        }
    }

    fun isCurrentlyPlaying(): Boolean = isPlaybackActive

    fun getCurrentPosition(): Int {
        return try {
            if (mediaPlayer != null && isPlaybackActive) {
                mediaPlayer?.currentPosition ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Error getting current position", e)
            0
        }
    }

    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Error getting duration", e)
            0
        }
    }

    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
            Log.d("VoicePlayer", "Seeked to position: $position")
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Error seeking", e)
        }
    }

    fun release() {
        Log.d("VoicePlayer", "Releasing VoicePlayer")
        stopAudio()
    }
}