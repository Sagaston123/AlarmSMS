package com.example.alarmasmsapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmaService : Service() {

    private var mp: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Aumentar volumen al máximo
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )

        // Vibración continua
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 1000, 1000) // espera 0, vibra 1000, pausa 1000
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            vibrator?.vibrate(pattern, 0)
        }

        // Sonido de alarma
        mp = MediaPlayer.create(this, R.raw.alarma_sonido)
        mp?.isLooping = true
        mp?.start()

        startForeground(1, createNotification())

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mp?.stop()
        mp?.release()
        vibrator?.cancel()
        Log.d("AlarmaService", "Alarma detenida correctamente")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "alarma_sms_channel"
        val channelName = "Alarma SMS"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal para Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal para alarma SMS"
            channel.setSound(null, null) // El sonido ya lo manejamos con MediaPlayer
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para apagar la alarma
        val stopIntent = Intent(this, StopServiceReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("2k estate atento")
            .setContentText("Alarma en ejecución")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.ic_launcher_foreground, "Apagar", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .build()
    }
}
