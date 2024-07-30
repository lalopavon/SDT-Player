package com.sdt.sdtplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val videoViewSplash: VideoView = findViewById(R.id.videoViewSplash)

        // Configurar el VideoView para reproducir el video
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.splash_video)
        videoViewSplash.setVideoURI(videoUri)
        videoViewSplash.setOnCompletionListener {
            // Iniciar VideoPlayerActivity cuando el video termine
            val intent = Intent(this, VideoPlayerActivity::class.java)
            startActivity(intent)
            finish()
        }
        videoViewSplash.start()

        // Si el video dura más de lo esperado, iniciar VideoPlayerActivity después de 10 segundos como medida de seguridad
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                val intent = Intent(this, VideoPlayerActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 10000) // 10000 milliseconds = 10 seconds
    }
}