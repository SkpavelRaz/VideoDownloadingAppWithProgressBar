package com.example.videodownloadapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.log


class DownloadService: Service() {
    private val channelId = "download_channel"
    private val notificationId = 101
    val STORAGE_DIR = "/Download/TestFolder"

    private var videoUrl = "http://dropbox.sandbox2000.com/intrvw/SampleVideo_1280x720_30mb.mp4"
//    private var videoUrl =
//        "https://firebasestorage.googleapis.com/v0/b/videodownloader-2847e.appspot.com/o/demo.mp4?alt=media&token=09adc942-0e7f-43b9-a7c2-54251e8d3776"


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startDownload(videoUrl)
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startDownload(videoUrl: String) {
        val storageDirectory = Environment.getExternalStorageDirectory()
            .toString() + STORAGE_DIR + "${
            System.currentTimeMillis().toString().replace(":", ".")
        }.mp4"
        val file = File(Environment.getExternalStorageDirectory().toString() + STORAGE_DIR)
        if (!file.exists()) {
            file.mkdirs()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url(videoUrl).build()
                val client = OkHttpClient()

                val response = client.newCall(request).execute()
                if (response.isSuccessful){
                    response.body?.let {connection->
                        val fileSize = connection.contentLength()
                        val inputStream = connection.byteStream()
                        val outputStream = FileOutputStream(storageDirectory)
                        var bytesCopied: Long = 0
                        val buffer = ByteArray(1024)
                        var byts = inputStream.read(buffer)
                        while (byts >= 0) {
                            bytesCopied += byts
                            val progress = (bytesCopied.toFloat() / fileSize.toFloat() * 100).toInt()
                            updateProgress(progress)
                            updateNotification(progress)
                            outputStream.write(buffer, 0, byts)
                            byts = inputStream.read(buffer)
                        }
                        outputStream.close()
                        inputStream.close()
                    }
                }else{
                    Toast.makeText(this@DownloadService, "failed download", Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                updateToast(e.message)
            }

        }
    }

    private fun updateToast(message: String?) {
        val intent = Intent("DOWNLOAD_PROGRESS")
        intent.putExtra("toast", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "test video"
            val descriptionText ="Downloading"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(progress: Int) {
        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Download in progress")
            .setContentText("$progress% downloaded")
            .setSmallIcon(R.drawable.ic_launcher_background)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@DownloadService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId, builder.build())
        }
    }

    // Update progress method
    private fun updateProgress(progress: Int) {
        val intent = Intent("DOWNLOAD_PROGRESS")
        intent.putExtra("progress", progress)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}