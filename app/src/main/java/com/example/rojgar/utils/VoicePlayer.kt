package com.example.rojgar.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

class VoicePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaybackActive = false
    private var currentAudioUrl: String? = null

    fun playAudio(
        audioUrl: String,
        onCompletion: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            // Stop current playback if playing
            if (isPlaybackActive && currentAudioUrl == audioUrl) {
                stopAudio()
                return
            } else if (isPlaybackActive) {
                stopAudio()
            }

            currentAudioUrl = audioUrl
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, audioUrl.toUri())
                setOnCompletionListener {
                    this@VoicePlayer.isPlaybackActive = false
                    currentAudioUrl = null
                    onCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    onError("Playback error: $what, $extra")
                    this@VoicePlayer.isPlaybackActive = false
                    currentAudioUrl = null
                    true
                }
                prepare()
                start()
                this@VoicePlayer.isPlaybackActive = true
            }
        } catch (e: Exception) {
            onError("Failed to play audio: ${e.message}")
            isPlaybackActive = false
            currentAudioUrl = null
        }
    }

    fun playLocalFile(
        file: File,
        onCompletion: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        playAudio(Uri.fromFile(file).toString(), onCompletion, onError)
    }

    fun stopAudio() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            // Ignore errors during stop
        } finally {
            mediaPlayer = null
            isPlaybackActive = false
            currentAudioUrl = null
        }
    }

    fun pauseAudio() {
        try {
            mediaPlayer?.pause()
            isPlaybackActive = false
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun resumeAudio() {
        try {
            mediaPlayer?.start()
            isPlaybackActive = true
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun isCurrentlyPlaying() = isPlaybackActive

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun release() {
        stopAudio()
    }
}
