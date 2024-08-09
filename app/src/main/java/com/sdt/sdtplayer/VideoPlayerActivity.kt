package com.sdt.sdtplayer

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var loadingSpinner: ProgressBar
    private val channels = listOf(
        "http://live-hls-web-aje.getaj.net/AJE/01.m3u8",
        "http://content.uplynk.com/channel/65812a0604044ab4b4e13d5911f13953.m3u8",
        "http://content.uplynk.com/channel/5f9f805ff3c44a02929bd58dc044e94c.m3u8"
    )
    private var currentChannelIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        playerView = findViewById(R.id.player_view)
        loadingSpinner = findViewById(R.id.loading_spinner)

        setupPlayer()

        // Reproducir el primer canal automáticamente
        playChannel(channels[currentChannelIndex])
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        playerView.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                moveToNextChannel()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                    loadingSpinner.visibility = View.GONE
                } else if (playbackState == Player.STATE_BUFFERING) {
                    loadingSpinner.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun playChannel(url: String) {
        loadingSpinner.visibility = View.VISIBLE
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private fun moveToNextChannel() {
        currentChannelIndex = (currentChannelIndex + 1) % channels.size
        if (currentChannelIndex == 0) {
            player.pause()
            loadingSpinner.visibility = View.GONE
        } else {
            playChannel(channels[currentChannelIndex])
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    // Rebobinar 10 segundos
                    player.seekTo(player.currentPosition - 10000)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    // Adelantar 10 segundos
                    player.seekTo(player.currentPosition + 10000)
                    return true
                }
                KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_DPAD_UP -> {
                    // Cambiar al siguiente canal
                    changeChannel(1)
                    return true
                }
                KeyEvent.KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_DPAD_DOWN -> {
                    // Cambiar al canal anterior
                    changeChannel(-1)
                    return true
                }
                KeyEvent.KEYCODE_BACK -> {
                    finish()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun changeChannel(increment: Int) {
        currentChannelIndex = (currentChannelIndex + increment + channels.size) % channels.size
        playChannel(channels[currentChannelIndex])
    }
}
