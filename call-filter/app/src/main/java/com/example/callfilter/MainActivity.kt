package com.example.callfilter

import android.app.role.RoleManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {

    private var roleHeld by mutableStateOf(false)

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        refreshRole()
        val msg = if (result.resultCode == RESULT_OK) "Filtro activado" else "Permiso denegado"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshRole()
        if (!roleHeld) requestRole()
        val db = FilterDbHelper.get(this)
        setContent {
            CallFilterAppTheme {
                AppScreen(db, roleHeld) { requestRole() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshRole()
    }

    private fun refreshRole() {
        val rm = getSystemService(RoleManager::class.java)
        roleHeld = rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun requestRole() {
        val rm = getSystemService(RoleManager::class.java)
        if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
            !rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        ) {
            roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
        }
    }
}

private enum class NavTab(val label: String, val title: String, val icon: ImageVector) {
    HOME("Inicio", "Filtro de llamadas", Icons.Default.Home),
    RULES("Reglas", "Mis reglas", Icons.Default.Lock),
    LOG("Registro", "Actividad", Icons.Default.DateRange),
    TEST("Pruebas", "Simulador", Icons.Default.PlayArrow)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(db: FilterDbHelper, roleActive: Boolean, onActivate: () -> Unit) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    val current = NavTab.entries[tab]

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(CsvManager.export(db.getRules()).toByteArray())
                }
            }.onSuccess {
                Toast.makeText(context, "Exportado", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Error al exportar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()
                CsvManager.import(text, db)
            }.onSuccess { r ->
                Toast.makeText(
                    context, "Importadas ${r.added}, omitidas ${r.skipped}", Toast.LENGTH_LONG
                ).show()
                refreshKey++
            }.onFailure {
                Toast.makeText(context, "Error al importar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current.title, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (current == NavTab.RULES) {
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("Importar CSV") },
                                    onClick = {
                                        menuOpen = false
                                        importLauncher.launch(arrayOf("*/*"))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Exportar CSV") },
                                    onClick = {
                                        menuOpen = false
                                        exportLauncher.launch("filtros.csv")
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavTab.entries.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            key(tab, refreshKey) {
                when (current) {
                    NavTab.HOME -> HomeScreen(db, roleActive, onActivate)
                    NavTab.RULES -> RulesScreen(db)
                    NavTab.LOG -> LogScreen(db)
                    NavTab.TEST -> TestScreen(db)
                }
            }
        }
    }
}