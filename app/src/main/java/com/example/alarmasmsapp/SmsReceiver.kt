package com.example.alarmasmsapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle? = intent.extras
        try {
            if (bundle != null) {
                val pdus = bundle.get("pdus") as? Array<*>
                pdus?.forEach { pdu ->
                    val format = bundle.getString("format")
                    val sms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        SmsMessage.createFromPdu(pdu as ByteArray, format)
                    } else {
                        SmsMessage.createFromPdu(pdu as ByteArray)
                    }

                    val message = sms.messageBody
                    val sender = sms.originatingAddress

                    Log.d("SmsReceiver", "SMS de: $sender - Contenido: $message")
                    Toast.makeText(context, "SMS recibido: $message", Toast.LENGTH_SHORT).show()

                    if (message.contains("ALERTA", true)) {
                        Log.d("SmsReceiver", "🔔 Mensaje de alarma detectado")
                        Toast.makeText(context, "🚨 ALERTA: $message", Toast.LENGTH_SHORT).show()

                        val intentService = Intent(context, AlarmaService::class.java)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intentService)
                        } else {
                            context.startService(intentService)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error procesando SMS", e)
        }
    }
}
