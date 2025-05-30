package com.example.alarmasmsapp

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.alarmasmsapp.ui.theme.AlarmaSmsAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 2)
        }
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

        val prefs = getSharedPreferences("eventos", Context.MODE_PRIVATE)

        setContent {
            AppContent(prefs)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(prefs: SharedPreferences) {
    var darkTheme by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("Inicio") }
    val drawerItems = listOf("Inicio", "Historial", "Perfil", "Tema")
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    AlarmaSmsAppTheme(darkTheme = darkTheme) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxHeight().background(Brush.verticalGradient(listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))))
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item) },
                            selected = selectedItem == item,
                            onClick = {
                                selectedItem = item
                                scope.launch { drawerState.close() }
                            },
                            icon = {
                                val icon = when (item) {
                                    "Inicio" -> Icons.Default.Home
                                    "Historial" -> Icons.Default.List
                                    "Perfil" -> Icons.Default.Person
                                    "Tema" -> Icons.Default.Settings
                                    else -> Icons.Default.Info
                                }
                                Icon(imageVector = icon, contentDescription = item)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Divider()
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("made by: github.com/Sagaston123", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Alarma SMS App") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (selectedItem) {
                        "Inicio" -> PantallaPrincipal(prefs)
                        "Historial" -> PantallaHistorial(prefs)
                        "Perfil" -> PantallaPerfil()
                        "Tema" -> PantallaTema { darkTheme = !darkTheme }
                    }
                }
            }
        }
    }
}
@Composable
fun PantallaHistorial(prefs: SharedPreferences) {
    val eventos = prefs.getStringSet("log", emptySet())?.toList()?.sortedDescending() ?: listOf()
    var filtro by remember { mutableStateOf("Todos") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial de Eventos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Todos", "ALARMA", "3103 ARMAR", "3103 DESARMAR").forEach { tipo ->
                FilterChip(
                    selected = filtro == tipo,
                    onClick = { filtro = tipo },
                    label = { Text(tipo) },
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
            items(eventos.filter { filtro == "Todos" || it.startsWith(filtro) }) { evento ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        text = evento,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
fun PantallaPerfil() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👤 Perfil del Usuario", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nombre: Gastón")
        Text("Teléfono vinculado: 1123456789")
        Text("Estado del sistema: ARMADO")
    }
}

@Composable
fun PantallaTema(onToggle: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎨 Tema de la app", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onToggle) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cambiar tema")
        }
    }
}

@Composable
fun PantallaPrincipal(prefs: SharedPreferences) {
    val context = LocalContext.current
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFF176)),
        start = Offset(0f, 0f),
        end = Offset.Infinite
    )

    fun enviarSMS(mensaje: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage("1123456789", null, mensaje, null, null)
            val log = prefs.getStringSet("log", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            log.add("${mensaje.uppercase()} ${System.currentTimeMillis()}")
            prefs.edit().putStringSet("log", log).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                context.startService(Intent(context, AlarmaService::class.java))
            },
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Text("🧪 Simular Alarma")
        }
        Button(
            onClick = {
                context.stopService(Intent(context, AlarmaService::class.java))
            },
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Text("🚩 Apagar Alarma")
        }
        Button(
            onClick = {
                AlertDialog.Builder(context)
                    .setTitle("¿Estás seguro?")
                    .setMessage("¿Deseás enviar: '3103 armar'?")
                    .setPositiveButton("Sí") { _, _ -> enviarSMS("3103 armar") }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Text("🔐 Armar sistema")
        }
        Button(
            onClick = {
                AlertDialog.Builder(context)
                    .setTitle("¿Estás seguro?")
                    .setMessage("¿Deseás enviar: '3103 desarmar'?")
                    .setPositiveButton("Sí") { _, _ -> enviarSMS("3103 desarmar") }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Text("🔓 Desarmar sistema")
        }
    }
}
