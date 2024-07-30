package com.sdt.sdtplayer

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var channelList: RecyclerView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var topBar: View
    private val channels = mutableListOf(
        "http://live-hls-web-aje.getaj.net/AJE/01.m3u8",
        "http://content.uplynk.com/channel/65812a0604044ab4b4e13d5911f13953.m3u8",
        "http://content.uplynk.com/channel/5f9f805ff3c44a02929bd58dc044e94c.m3u8"
    )
    private lateinit var adapter: UrlAdapter
    private var currentChannelIndex = 0

    private val hideChannelListHandler = Handler(Looper.getMainLooper())
    private val hideChannelListRunnable = Runnable {
        hideChannelList()
    }

    private var isChannelListVisible = false
    private var isPasswordDialogVisible = false

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
        }, 5000)

        setupPlayer()
        setupChannelList()

        // Mostrar la guía al iniciar la actividad
        showChannelList()
        // Ocultar la guía automáticamente después de unos segundos al iniciar la actividad
        hideChannelListAfterDelay()

        // Reproducir el primer canal automáticamente
        playChannel(channels[0])
        adapter.setSelectedPosition(0)
        currentChannelIndex = 0
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
                    hideChannelListAfterDelay() // Ocultar la guía al comenzar la reproducción
                } else if (playbackState == Player.STATE_BUFFERING) {
                    loadingSpinner.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupChannelList() {
        channelList.layoutManager = LinearLayoutManager(this)
        adapter = UrlAdapter(
            channels.mapIndexed { index, _ -> "Canal ${index + 1}" }.toMutableList(),
            false
        ) { position ->
            currentChannelIndex = position
            adapter.setSelectedPosition(currentChannelIndex)
            // Desplazar la lista de canales para mantener la selección visible
            channelList.scrollToPosition(currentChannelIndex)
        }
        channelList.adapter = adapter
    }

    private fun playChannel(url: String) {
        loadingSpinner.visibility = View.VISIBLE
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        hideChannelListAfterDelay()
    }

    private fun moveToNextChannel() {
        currentChannelIndex = (currentChannelIndex + 1) % channels.size
        if (currentChannelIndex == 0) {
            // Si hemos intentado todos los canales y ninguno funciona, pausar el reproductor
            player.pause()
            loadingSpinner.visibility = View.GONE
        } else {
            playChannel(channels[currentChannelIndex])
        }
    }

    private fun hideChannelListAfterDelay() {
        hideChannelListHandler.removeCallbacks(hideChannelListRunnable)
        hideChannelListHandler.postDelayed(hideChannelListRunnable, 5000)
    }

    private fun showChannelList() {
        if (!isChannelListVisible) {
            isChannelListVisible = true
            channelList.visibility = View.VISIBLE
            channelList.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_from_right))
            playerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_out))
            playerView.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                weight = 1f
            }
        }
        hideChannelListAfterDelay() // Ocultar la guía después de unos segundos
    }

    private fun hideChannelList() {
        if (isChannelListVisible) {
            isChannelListVisible = false
            channelList.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_to_right))
            playerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in))
            channelList.visibility = View.GONE
            playerView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
    }

    private fun changeChannel(increment: Int) {
        currentChannelIndex = (currentChannelIndex + increment + channels.size) % channels.size
        playChannel(channels[currentChannelIndex])
    }

    private fun selectChannel() {
        adapter.setSelectedPosition(currentChannelIndex)
        channelList.scrollToPosition(currentChannelIndex)
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
                    if (isChannelListVisible) {
                        // Reproducir el canal seleccionado
                        playChannel(channels[adapter.getSelectedPosition()])
                        hideChannelList()
                    } else {
                        // Mostrar la guía de programación
                        showChannelList()
                    }
                    return true
                }
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
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (isChannelListVisible) {
                        // Navegar hacia arriba en la lista de canales
                        adapter.setSelectedPosition((adapter.getSelectedPosition() - 1 + channels.size) % channels.size)
                        channelList.scrollToPosition(adapter.getSelectedPosition())
                    } else {
                        // Cambiar al canal anterior
                        changeChannel(-1)
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (isChannelListVisible) {
                        // Navegar hacia abajo en la lista de canales
                        adapter.setSelectedPosition((adapter.getSelectedPosition() + 1) % channels.size)
                        channelList.scrollToPosition(adapter.getSelectedPosition())
                    } else {
                        // Cambiar al siguiente canal
                        changeChannel(1)
                    }
                    return true
                }
                KeyEvent.KEYCODE_CHANNEL_UP -> {
                    // Cambiar al siguiente canal
                    changeChannel(1)
                    return true
                }
                KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                    // Cambiar al canal anterior
                    changeChannel(-1)
                    return true
                }
                KeyEvent.KEYCODE_BACK -> {
                    showPasswordDialog()
                    return true
                }
                KeyEvent.KEYCODE_HOME -> {
                    showPasswordDialog()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun showPasswordDialog() {
        if (isPasswordDialogVisible) return

        isPasswordDialogVisible = true
        val passwordEditText = EditText(this)
        passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Enter Password")
            .setView(passwordEditText)
            .setPositiveButton("OK") { dialog, _ ->
                val password = passwordEditText.text.toString()
                if (password == "SDT123.com") {
                    finish()
                } else {
                    // Show an error message or do something else if the password is incorrect
                }
                isPasswordDialogVisible = false
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                isPasswordDialogVisible = false

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                isPasswordDialogVisible = false
                dialog.dismiss()
            }
            .setOnDismissListener {
                isPasswordDialogVisible = false
            }
            .show()
    }

    override fun onUserLeaveHint() {
        // This method is called when the user presses the home button
        showPasswordDialog()
    }
}
