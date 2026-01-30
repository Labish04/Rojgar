package com.example.rojgar.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Diagnostic utility to test voice message functionality
 * Use this to identify issues with recording, uploading, or playback
 */
object VoiceDiagnostic {

    private const val TAG = "VoiceDiagnostic"

    /**
//     * Test if a local audio file is valid and playable
     */
    fun testLocalAudioFile(file: File): DiagnosticResult {
        val issues = mutableListOf<String>()
        val info = mutableListOf<String>()

        // Check file existence
        if (!file.exists()) {
            issues.add("❌ File does not exist")
            return DiagnosticResult(false, issues, info)
        }
        info.add("✓ File exists")

        // Check file size
        val fileSize = file.length()
        if (fileSize == 0L) {
            issues.add("❌ File is empty (0 bytes)")
            return DiagnosticResult(false, issues, info)
        }
        info.add("✓ File size: ${fileSize / 1024}KB")

        // Check file readability
        if (!file.canRead()) {
            issues.add("❌ File cannot be read (permission issue)")
            return DiagnosticResult(false, issues, info)
        }
        info.add("✓ File is readable")

        // Check file extension
        val extension = file.extension.lowercase()
        if (extension !in listOf("mp3", "m4a", "aac", "wav", "ogg")) {
            issues.add("⚠️ Unusual audio format: $extension")
        } else {
            info.add("✓ Valid audio format: $extension")
        }

        // Try to get duration with MediaPlayer
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration
            mediaPlayer.release()

            if (duration > 0) {
                info.add("✓ Audio duration: ${duration / 1000} seconds")
                info.add("✓ File is playable by MediaPlayer")
            } else {
                issues.add("⚠️ MediaPlayer returned 0 duration")
            }
        } catch (e: Exception) {
            issues.add("❌ MediaPlayer cannot play file: ${e.message}")
            Log.e(TAG, "MediaPlayer test failed", e)
        }

        val isValid = issues.isEmpty() || issues.all { it.startsWith("⚠️") }
        return DiagnosticResult(isValid, issues, info)
    }

    /**
     * Test if a URL is accessible and returns audio content
     */
    fun testAudioUrl(audioUrl: String): DiagnosticResult {
        val issues = mutableListOf<String>()
        val info = mutableListOf<String>()

        // Check URL format
        if (!audioUrl.startsWith("http://") && !audioUrl.startsWith("https://")) {
            issues.add("❌ Invalid URL format (must start with http:// or https://)")
            return DiagnosticResult(false, issues, info)
        }
        info.add("✓ Valid URL format")

        // Check HTTPS (Cloudinary should use HTTPS)
        if (!audioUrl.startsWith("https://")) {
            issues.add("⚠️ Using HTTP instead of HTTPS")
        } else {
            info.add("✓ Using HTTPS")
        }

        // Test URL accessibility
        try {
            val url = URL(audioUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            val responseCode = connection.responseCode
            val contentType = connection.contentType
            val contentLength = connection.contentLength

            connection.disconnect()

            if (responseCode == 200) {
                info.add("✓ URL is accessible (HTTP 200)")
            } else {
                issues.add("❌ URL returned HTTP $responseCode")
            }

            if (contentType != null) {
                info.add("✓ Content-Type: $contentType")
                if (!contentType.contains("audio") && !contentType.contains("video")) {
                    issues.add("⚠️ Content-Type is not audio/video")
                }
            }

            if (contentLength > 0) {
                info.add("✓ Content-Length: ${contentLength / 1024}KB")
            } else {
                issues.add("⚠️ Content-Length is unknown or 0")
            }

        } catch (e: Exception) {
            issues.add("❌ Cannot access URL: ${e.message}")
            Log.e(TAG, "URL test failed", e)
            return DiagnosticResult(false, issues, info)
        }

        // Test if MediaPlayer can prepare the URL
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration
            mediaPlayer.release()

            if (duration > 0) {
                info.add("✓ MediaPlayer can play URL (duration: ${duration / 1000}s)")
            } else {
                issues.add("⚠️ MediaPlayer prepared but duration is 0")
            }
        } catch (e: Exception) {
            issues.add("❌ MediaPlayer cannot prepare URL: ${e.message}")
            Log.e(TAG, "MediaPlayer URL test failed", e)
        }

        val isValid = issues.isEmpty() || issues.all { it.startsWith("⚠️") }
        return DiagnosticResult(isValid, issues, info)
    }

    /**
     * Test recording capabilities
     */
    fun testRecordingCapabilities(context: Context): DiagnosticResult {
        val issues = mutableListOf<String>()
        val info = mutableListOf<String>()

        // Check if cache directory is available
        val cacheDir = context.cacheDir
        if (cacheDir.exists() && cacheDir.canWrite()) {
            info.add("✓ Cache directory is writable")
        } else {
            issues.add("❌ Cannot write to cache directory")
        }

        // Test creating a file in cache
        try {
            val testFile = File(cacheDir, "test_${System.currentTimeMillis()}.txt")
            testFile.writeText("test")
            val canRead = testFile.canRead()
            testFile.delete()

            if (canRead) {
                info.add("✓ Can create and read files in cache")
            } else {
                issues.add("❌ Cannot read files from cache")
            }
        } catch (e: Exception) {
            issues.add("❌ File I/O test failed: ${e.message}")
        }

        val isValid = issues.isEmpty()
        return DiagnosticResult(isValid, issues, info)
    }

    /**
     * Complete diagnostic report
     */
    fun runFullDiagnostic(
        context: Context,
        audioFile: File? = null,
        audioUrl: String? = null
    ): String {
        val report = StringBuilder()
        report.appendLine("=== Voice Message Diagnostic Report ===")
        report.appendLine()

        // Test recording capabilities
        report.appendLine("1. Recording Capabilities:")
        val recordingTest = testRecordingCapabilities(context)
        recordingTest.info.forEach { report.appendLine("   $it") }
        recordingTest.issues.forEach { report.appendLine("   $it") }
        report.appendLine()

        // Test local file if provided
        audioFile?.let { file ->
            report.appendLine("2. Local Audio File Test:")
            report.appendLine("   File: ${file.absolutePath}")
            val fileTest = testLocalAudioFile(file)
            fileTest.info.forEach { report.appendLine("   $it") }
            fileTest.issues.forEach { report.appendLine("   $it") }
            report.appendLine()
        }

        // Test URL if provided
        audioUrl?.let { url ->
            report.appendLine("3. Audio URL Test:")
            report.appendLine("   URL: $url")
            val urlTest = testAudioUrl(url)
            urlTest.info.forEach { report.appendLine("   $it") }
            urlTest.issues.forEach { report.appendLine("   $it") }
            report.appendLine()
        }

        report.appendLine("=== End of Report ===")

        val fullReport = report.toString()
        Log.d(TAG, fullReport)
        return fullReport
    }

    data class DiagnosticResult(
        val isValid: Boolean,
        val issues: List<String>,
        val info: List<String>
    )
}

// Extension function to easily run diagnostics from anywhere
fun File.diagnoseAudio(): VoiceDiagnostic.DiagnosticResult {
    return VoiceDiagnostic.testLocalAudioFile(this)
}

fun String.diagnoseAudioUrl(): VoiceDiagnostic.DiagnosticResult {
    return VoiceDiagnostic.testAudioUrl(this)
}