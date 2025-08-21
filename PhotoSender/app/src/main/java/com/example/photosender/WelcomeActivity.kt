package com.example.photosender

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.photosender.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if settings are already configured.
        val sharedPref = getSharedPreferences("PhotoSenderPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TELEGRAM_BOT_TOKEN", null)
        val chatId = sharedPref.getString("TELEGRAM_CHAT_ID", null)

        if (token != null && !token.isNullOrBlank() && chatId != null && !chatId.isNullOrBlank()) {
            // If configured, go straight to MainActivity
            navigateToMain()
            return
        }

        // If not configured, show the welcome screen.
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goToSettingsButton.setOnClickListener {
            // Navigate to Settings and finish this activity so the user comes back to a fresh check
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish WelcomeActivity so user can't go back to it
    }
}
