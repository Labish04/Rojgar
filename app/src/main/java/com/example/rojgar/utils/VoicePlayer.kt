package com.example.rojgar.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
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
            Log.d("VoicePlayer", "Attempting to play audio: $audioUrl")

            // If already playing the same audio, stop it
            if (isPlaybackActive && currentAudioUrl == audioUrl) {
                Log.d("VoicePlayer", "Already playing this audio, stopping...")
                stopAudio()
                return
            }
            // If playing different audio, stop the current one first
            else if (isPlaybackActive) {
                Log.d("VoicePlayer", "Stopping current audio before playing new one")
                stopAudio()
            }

            currentAudioUrl = audioUrl

            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                // Set data source
                setDataSource(context, audioUrl.toUri())

                // Set completion listener
                setOnCompletionListener {
                    Log.d("VoicePlayer", "Playback completed")
                    this@VoicePlayer.isPlaybackActive = false
                    currentAudioUrl = null
                    onCompletion()
                }

                // Set error listener
                setOnErrorListener { mp, what, extra ->
                    Log.e("VoicePlayer", "Playback error - what: $what, extra: $extra")
                    onError("Playback error: $what, $extra")
                    this@VoicePlayer.isPlaybackActive = false
                    currentAudioUrl = null
                    true
                }

                // Prepare and start
                prepareAsync()
                setOnPreparedListener { mp ->
                    Log.d("VoicePlayer", "MediaPlayer prepared, starting playback...")
                    mp.start()
                    this@VoicePlayer.isPlaybackActive = true
                    Log.d("VoicePlayer", "Playback started successfully")
                }
            }

        } catch (e: Exception) {
            Log.e("VoicePlayer", "Failed to play audio", e)
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
        if (!file.exists()) {
            Log.e("VoicePlayer", "Local file does not exist: ${file.absolutePath}")
            onError("File does not exist")
            return
        }

        Log.d("VoicePlayer", "Playing local file: ${file.absolutePath}")
        playAudio(Uri.fromFile(file).toString(), onCompletion, onError)
    }

    fun stopAudio() {
        try {
            Log.d("VoicePlayer", "Stopping audio playback")

            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    Log.d("VoicePlayer", "MediaPlayer stopped")
                }
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
            mediaPlayer?.currentPosition ?: 0
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