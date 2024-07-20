package com.sdt.sdtplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var player: SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var channelList: RecyclerView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var topBar: View
    private val channels = mutableListOf<String>()
    private lateinit var adapter: UrlAdapter  // Cambia ChannelAdapter a UrlAdapter
    private var currentChannelIndex = 0

    private val hideChannelListHandler = Handler(Looper.getMainLooper())
    private val hideChannelListRunnable = Runnable {
        channelList.visibility = View.GONE
        playerView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        playerView = findViewById(R.id.player_view)
        channelList = findViewById(R.id.channel_list)
        loadingSpinner = findViewById(R.id.loading_spinner)
        topBar = findViewById(R.id.top_bar)

        // Ocultar la barra superior después de 5 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            topBar.visibility = View.GONE
        }, 5000) // 5000 milliseconds = 5 seconds

        setupPlayer()
        setupChannelList()
        handleIntent()
        hideChannelListAfterDelay()
    }

    private fun setupPlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player
        playerView.resizeMode = com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                showErrorAndReturn()
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                if (state == Player.STATE_READY || state == Player.STATE_ENDED) {
                    loadingSpinner.visibility = View.GONE
                } else if (state == Player.STATE_BUFFERING) {
                    loadingSpinner.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupChannelList() {
        channelList.layoutManager = LinearLayoutManager(this)
        // Usar UrlAdapter en lugar de ChannelAdapter y pasar false para no mostrar el botón de eliminación
        adapter = UrlAdapter(channels, false) { }
        channelList.adapter = adapter
    }

    private fun handleIntent() {
        val urls = intent.getStringArrayListExtra("VIDEO_URLS")
        if (urls != null) {
            channels.addAll(urls)
            adapter.notifyDataSetChanged()
            if (channels.isNotEmpty()) {
                playChannel(channels[0])
                adapter.setSelectedPosition(0)
                currentChannelIndex = 0
            }
        }
    }

    private fun playChannel(url: String) {
        loadingSpinner.visibility = View.VISIBLE
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private fun showErrorAndReturn() {
        loadingSpinner.visibility = View.GONE
        player.stop()
        val intent = Intent(this, UrlInputActivity::class.java).apply {
            putExtra("ERROR_MESSAGE", "Failed to load video. Please check the URL and try again.")
        }
        startActivity(intent)
        finish()
    }

    private fun hideChannelListAfterDelay() {
        hideChannelListHandler.removeCallbacks(hideChannelListRunnable)
        hideChannelListHandler.postDelayed(hideChannelListRunnable, 5000)
    }

    private fun showChannelList() {
        channelList.visibility = View.VISIBLE
        playerView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT
        ).apply {
            weight = 1f
        }
        hideChannelListAfterDelay()
    }

    private fun changeChannel(increment: Int) {
        currentChannelIndex = (currentChannelIndex + increment + channels.size) % channels.size
        playChannel(channels[currentChannelIndex])
        adapter.setSelectedPosition(currentChannelIndex)
        showChannelList()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    // Toggle play/pause
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    // Rewind 10 seconds
                    player.seekTo(player.currentPosition - 10000)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    // Fast forward 10 seconds
                    player.seekTo(player.currentPosition + 10000)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_LEFT -> {
                    // Show channel list
                    showChannelList()
                    return true
                }
                KeyEvent.KEYCODE_CHANNEL_UP -> {
                    // Change to next channel
                    changeChannel(1)
                    return true
                }
                KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                    // Change to previous channel
                    changeChannel(-1)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}