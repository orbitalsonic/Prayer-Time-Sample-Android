package com.orbitalsonic.prayertimesample.data.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import com.orbitalsonic.prayertimesample.R

/**
 * Centralized Azan playback using the single bundled asset [R.raw.azan].
 */
class AzanPlayerManager(private val context: Context) {

    private val appContext = context.applicationContext
    private val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var mediaPlayer: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null

    @Volatile
    private var playing: Boolean = false

    fun isAzanPlaying(): Boolean = playing

    /**
     * Plays the bundled Azan for any prayer in [AZAN] mode.
     * Stops any current playback first (e.g. overlapping prayer).
     */
    fun play() {
        stopInternal(releaseFocus = false)
        if (!requestAudioFocus()) return

        val player = MediaPlayer.create(appContext, R.raw.azan) ?: run {
            abandonAudioFocus()
            return
        }

        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        player.setOnCompletionListener { stop() }
        player.setOnErrorListener { _, _, _ ->
            stop()
            true
        }

        mediaPlayer = player
        try {
            player.start()
            playing = true
        } catch (_: Exception) {
            stop()
        }
    }

    /**
     * Stops Azan and releases [MediaPlayer] resources.
     * Called from Stop action, notification dismiss, and new prayer triggers.
     */
    fun stop() {
        stopInternal(releaseFocus = true)
    }

    private fun stopInternal(releaseFocus: Boolean) {
        mediaPlayer?.run {
            try {
                if (isPlaying) stop()
            } catch (_: Exception) {
                // Player may already be stopped/released
            }
            try {
                release()
            } catch (_: Exception) {
                // Ignore double-release
            }
        }
        mediaPlayer = null
        playing = false
        if (releaseFocus) {
            abandonAudioFocus()
        }
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> stop()
                    }
                }
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> stop()
                    }
                },
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        focusRequest = null
    }
}
