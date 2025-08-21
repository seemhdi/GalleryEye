package com.example.photosender

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class TelegramRepository(private val context: Context) {

    companion object {
        private const val TAG = "TelegramRepository"
        private const val BASE_URL = "https://api.telegram.org/"
    }

    private val telegramApi: TelegramApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            // Set to Level.BODY for full request/response logs, NONE for production
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TelegramApi::class.java)
    }

    suspend fun sendPhoto(uri: Uri) {
        val sharedPref = context.getSharedPreferences("PhotoSenderPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TELEGRAM_BOT_TOKEN", null)
        val chatId = sharedPref.getString("TELEGRAM_CHAT_ID", null)

        if (token.isNullOrBlank() || chatId.isNullOrBlank()) {
            Log.e(TAG, "Telegram Token or Chat ID is not configured.")
            return
        }

        // Copy the file from the content URI to a temporary file in the app's cache.
        // This is a robust way to handle Uris from the MediaStore.
        val tempFile = getFileFromUri(uri)
        if (tempFile == null) {
            Log.e(TAG, "Failed to create a temporary file from URI: $uri")
            return
        }

        try {
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", tempFile.name, requestFile)
            val chatIdPart = chatId.toRequestBody("text/plain".toMediaTypeOrNull())

            Log.d(TAG, "Uploading photo to chat ID: $chatId")
            val response = telegramApi.sendPhoto(token, chatIdPart, photoPart)

            if (response.isSuccessful) {
                Log.i(TAG, "Photo uploaded successfully.")
            } else {
                Log.e(TAG, "Failed to upload photo: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during photo upload", e)
        } finally {
            // Clean up the temporary file to save space
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(uri)
            // Use the cache directory for temporary files.
            val tempFile = File(context.cacheDir, fileName)
            tempFile.createNewFile()
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create temp file from URI", e)
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                       result = cursor.getString(displayNameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        // Provide a default name if all else fails
        return result ?: "temp_image_${System.currentTimeMillis()}.jpg"
    }
}
