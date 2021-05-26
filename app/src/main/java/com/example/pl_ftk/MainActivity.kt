package com.example.pl_ftk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var progressBar:ProgressBar
    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        progressBar = findViewById(R.id.pb)
        Handler().postDelayed(
            {
            progressBar.visibility = View.GONE
            },2000)
       Handler().postDelayed({
           val intent = Intent(this,HomeScreen::class.java)
           finish()
           startActivity(intent)
       },2000)

    }

}