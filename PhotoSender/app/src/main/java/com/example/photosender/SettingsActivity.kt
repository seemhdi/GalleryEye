package com.example.photosender

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.photosender.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Settings"

        val sharedPref = getSharedPreferences("PhotoSenderPrefs", Context.MODE_PRIVATE)

        // Load existing settings
        binding.tokenEditText.setText(sharedPref.getString("TELEGRAM_BOT_TOKEN", ""))
        binding.chatIdEditText.setText(sharedPref.getString("TELEGRAM_CHAT_ID", ""))

        binding.saveButton.setOnClickListener {
            val token = binding.tokenEditText.text.toString().trim()
            val chatId = binding.chatIdEditText.text.toString().trim()

            if (token.isBlank() || chatId.isBlank()) {
                Toast.makeText(this, "Token and Chat ID cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save settings
            with(sharedPref.edit()) {
                putString("TELEGRAM_BOT_TOKEN", token)
                putString("TELEGRAM_CHAT_ID", chatId)
                apply()
            }

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()

            // Navigate to Main Activity
            val intent = Intent(this, MainActivity::class.java)
            // Clear the activity stack and start MainActivity as a new task
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
