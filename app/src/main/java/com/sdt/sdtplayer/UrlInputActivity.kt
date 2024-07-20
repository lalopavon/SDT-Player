package com.sdt.sdtplayer

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UrlInputActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var addButton: Button
    private lateinit var playButton: Button
    private lateinit var clearButton: Button
    private lateinit var urlList: RecyclerView
    private val urls = mutableListOf<String>()
    private lateinit var adapter: UrlAdapter
    private var inDeleteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_input)

        urlInput = findViewById(R.id.url_input)
        addButton = findViewById(R.id.add_button)
        playButton = findViewById(R.id.play_button)
        clearButton = findViewById(R.id.clear_button)
        urlList = findViewById(R.id.url_list)

        setupUrlList()

        addButton.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotEmpty()) {
                addUrl(url)
                urlInput.text.clear()
                urlInput.requestFocus()
            }
        }

        clearButton.setOnClickListener {
            urlInput.text.clear()
            urlInput.requestFocus()
        }

        playButton.setOnClickListener {
            if (urls.isNotEmpty()) {
                val intent = Intent(this, VideoPlayerActivity::class.java).apply {
                    putStringArrayListExtra("VIDEO_URLS", ArrayList(urls))
                }
                startActivity(intent)
            }
        }

        // Show error message if any
        val errorMessage = intent.getStringExtra("ERROR_MESSAGE")
        if (!errorMessage.isNullOrEmpty()) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }

        // Add key listeners for better navigation control
        setupKeyListeners()
    }

    private fun setupUrlList() {
        urlList.layoutManager = LinearLayoutManager(this)
        adapter = UrlAdapter(urls, true) { position -> deleteUrl(position) }
        urlList.adapter = adapter
    }

    private fun addUrl(url: String) {
        urls.add(url)
        adapter.notifyItemInserted(urls.size - 1)
    }

    private fun deleteUrl(position: Int) {
        urls.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    private fun setupKeyListeners() {
        urlInput.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (urlInput.hasFocus()) {
                    clearButton.requestFocus()
                    return@setOnKeyListener true
                }
            }
            false
        }

        clearButton.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        urlInput.requestFocus()
                        return@setOnKeyListener true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        if (adapter.itemCount > 0) {
                            urlList.requestFocus()
                            return@setOnKeyListener true
                        }
                    }
                }
            }
            false
        }

        urlList.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        if (!inDeleteMode) {
                            inDeleteMode = true
                            val firstVisibleItemPosition = (urlList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            if (firstVisibleItemPosition != RecyclerView.NO_POSITION) {
                                urlList.findViewHolderForAdapterPosition(firstVisibleItemPosition)?.itemView?.findViewById<Button>(R.id.delete_channel)?.requestFocus()
                            }
                        } else {
                            inDeleteMode = false
                            urlList.clearFocus()
                            urlList.requestFocus()
                        }
                        return@setOnKeyListener true
                    }
                }
            }
            false
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    when {
                        urlInput.hasFocus() -> {
                            val url = urlInput.text.toString()
                            if (url.isNotEmpty()) {
                                addUrl(url)
                                urlInput.text.clear()
                            }
                        }
                        addButton.hasFocus() -> addButton.performClick()
                        clearButton.hasFocus() -> clearButton.performClick()
                        playButton.hasFocus() -> playButton.performClick()
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    when {
                        urlInput.hasFocus() -> addButton.requestFocus()
                        addButton.hasFocus() -> urlList.requestFocus()
                        urlList.hasFocus() && !inDeleteMode -> playButton.requestFocus()
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    when {
                        playButton.hasFocus() -> urlList.requestFocus()
                        urlList.hasFocus() && !inDeleteMode -> addButton.requestFocus()
                        addButton.hasFocus() -> urlInput.requestFocus()
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (clearButton.hasFocus()) {
                        urlInput.requestFocus()
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (urlInput.hasFocus()) {
                        clearButton.requestFocus()
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}