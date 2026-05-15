package com.example.sceneenglish.data.repository

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File
import androidx.media3.common.AudioAttributes as Media3AudioAttributes

class AudioPlayer(context: Context) {
    private var activeListener: Player.Listener? = null
    private val player = ExoPlayer.Builder(context.applicationContext).build().apply {
        setAudioAttributes(
            Media3AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build(),
            true
        )
        volume = 1.0f
    }

    fun play(file: File, onStarted: () -> Unit, onEnded: () -> Unit, onError: (Throwable) -> Unit) {
        player.stop()
        player.clearMediaItems()
        activeListener?.let(player::removeListener)
        activeListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> onStarted()
                    Player.STATE_ENDED -> onEnded()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                onError(error)
            }
        }
        player.addListener(activeListener!!)
        player.setMediaItem(MediaItem.fromUri(file.toUri()))
        player.prepare()
        player.play()
    }

    fun release() {
        activeListener?.let(player::removeListener)
        activeListener = null
        player.release()
    }
}
