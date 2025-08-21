package com.example.photosender

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.*

class PhotoMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: TelegramRepository
    private var contentObserver: ContentObserver? = null
    @Volatile private var lastProcessedTimestamp: Long = 0

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "PhotoSenderChannel"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "PhotoMonitorService"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't provide binding
    }

    override fun onCreate() {
        super.onCreate()
        repository = TelegramRepository(applicationContext)
        createNotificationChannel()
        val notification = createNotification("Initializing service...")
        startForeground(NOTIFICATION_ID, notification)

        // Load the last processed timestamp. Default to current time to avoid sending old photos on first run.
        val sharedPref = getSharedPreferences("PhotoSenderPrefs", Context.MODE_PRIVATE)
        lastProcessedTimestamp = sharedPref.getLong("LAST_PROCESSED_TIMESTAMP", System.currentTimeMillis() / 1000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service starting...")
        updateNotification("Monitoring for new photos...")
        registerContentObserver()
        return START_STICKY // If the service is killed, it will be automatically restarted
    }

    private fun registerContentObserver() {
        if (contentObserver == null) {
            // We need a handler to dispatch onChange events.
            val handler = Handler(Looper.getMainLooper())
            contentObserver = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    // A short delay can help batch notifications for multiple rapid saves
                    handler.removeCallbacksAndMessages(null) // Remove previous pending checks
                    handler.postDelayed({ checkForNewPhotos() }, 2000) // 2-second debounce
                }
            }
            contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver!!
            )
            Log.d(TAG, "ContentObserver registered.")
        }
    }

    private fun checkForNewPhotos() {
        Log.d(TAG, "Checking for new photos since timestamp: $lastProcessedTimestamp")
        serviceScope.launch {
            try {
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED
                )
                val selection = "${MediaStore.Images.Media.DATE_ADDED} > ?"
                val selectionArgs = arrayOf(lastProcessedTimestamp.toString())
                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} ASC" // Ascending to process in order

                contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    if (cursor.count > 0) {
                        Log.d(TAG, "Found ${cursor.count} new photos.")
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                        var latestTimestamp = lastProcessedTimestamp

                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val name = cursor.getString(nameColumn)
                            val dateAdded = cursor.getLong(dateAddedColumn)
                            val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())

                            Log.i(TAG, "Processing new photo: $name, URI: $contentUri")
                            updateNotification("Uploading $name...")

                            repository.sendPhoto(contentUri)

                            Log.i(TAG, "Finished processing $name")
                            latestTimestamp = dateAdded
                        }

                        // After the loop, save the timestamp of the last processed photo
                        lastProcessedTimestamp = latestTimestamp
                        getSharedPreferences("PhotoSenderPrefs", Context.MODE_PRIVATE).edit()
                            .putLong("LAST_PROCESSED_TIMESTAMP", lastProcessedTimestamp)
                            .apply()

                        Log.d(TAG, "New lastProcessedTimestamp is: $lastProcessedTimestamp")
                        updateNotification("Monitoring for new photos...")
                    } else {
                        Log.d(TAG, "No new photos found.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for new photos", e)
                updateNotification("Error occurred. Check logs.")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Photo Sender Service Channel",
                NotificationManager.IMPORTANCE_LOW // Low importance to be less intrusive
            ).apply {
                description = "Channel for PhotoSender background service."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(text: String): Notification {
        // Add a pending intent to open the app when the notification is clicked
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("PhotoSender Service")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_camera) // Placeholder icon
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroying...")
        if (contentObserver != null) {
            contentResolver.unregisterContentObserver(contentObserver!!)
            contentObserver = null
        }
        serviceScope.cancel() // Cancel all coroutines when the service is destroyed
    }
}
