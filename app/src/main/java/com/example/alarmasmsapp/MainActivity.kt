package com.example.alarmasmsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.alarmasmsapp.ui.theme.AlarmaSmsAppTheme
import android.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("Esta app necesita acceso a los SMS para detectar alarmas automáticamente.")
                .setPositiveButton("Aceptar") { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), 1)
                }
                .setCancelable(false)
                .show()
        }



        setContent {
            AlarmaSmsAppTheme {
                AppContent(
                    onStopAlarm = {
                        stopService(Intent(this, AlarmaService::class.java))
                    },
                    onTestAlarm = {
                        startService(Intent(this, AlarmaService::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun AppContent(onStopAlarm: () -> Unit, onTestAlarm: () -> Unit) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFF176)),
        start = Offset(0f, 0f),
        end = Offset.Infinite
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onTestAlarm,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF81C784), // verde claro
                contentColor = Color.White
            )
        ) {
            Text("🧪 Simular Alarma")
        }

        Button(
            onClick = onStopAlarm,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFF176),
                contentColor = Color(0xFF212121)
            )
        ) {
            Text("🛑 Apagar Alarma")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppContent() {
    AlarmaSmsAppTheme {
        AppContent({}, {})
    }
}
