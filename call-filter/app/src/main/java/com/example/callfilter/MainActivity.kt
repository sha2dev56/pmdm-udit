package com.example.callfilter

import android.app.role.RoleManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var themePreferences: ThemePreferences
    private var roleHeld by mutableStateOf(false)
    private var resumeTick by mutableIntStateOf(0)
    private var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshRole()
        val message = if (roleHeld) "Protección activada" else "Permiso no concedido"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        themePreferences = ThemePreferences(this)
        themeMode = themePreferences.load()
        refreshRole()
        val db = FilterDbHelper.get(this)
        setContent {
            CallFilterAppTheme(themeMode = themeMode) {
                AppScreen(
                    db = db,
                    roleActive = roleHeld,
                    refreshSignal = resumeTick,
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        themeMode = mode
                        themePreferences.save(mode)
                    },
                    onActivate = { requestRole() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshRole()
        resumeTick++
    }

    private fun refreshRole() {
        val roleManager = getSystemService(RoleManager::class.java)
        roleHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun requestRole() {
        val roleManager = getSystemService(RoleManager::class.java)
        when {
            roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ->
                Toast.makeText(this, "El filtro ya está activo", Toast.LENGTH_SHORT).show()

            roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) ->
                roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))

            else -> Toast.makeText(
                this,
                "Este dispositivo no permite filtrar llamadas",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

private enum class NavTab(
    val label: String,
    val title: String,
    val subtitle: String?,
    val icon: ImageVector,
    val inBottomBar: Boolean = true
) {
    HOME("Inicio", "CallFilter", null, Icons.Default.Home),
    RULES(
        "Listas",
        "Bloqueos y permitidos",
        "Prefijos, números y excepciones",
        Icons.Default.Lock
    ),
    ACTIVITY(
        "Actividad",
        "Actividad reciente",
        "Llamadas revisadas por el filtro",
        Icons.Default.Notifications
    ),
    SETTINGS(
        "Ajustes",
        "Ajustes",
        "Apariencia, privacidad y datos",
        Icons.Default.Settings
    ),
    TEST(
        "Simulador",
        "Simulador",
        "Prueba una llamada sin recibirla",
        Icons.Default.PlayArrow,
        inBottomBar = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    db: FilterDbHelper,
    roleActive: Boolean,
    refreshSignal: Int,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onActivate: () -> Unit
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val motionEnabled = rememberMotionEnabled()
    var routeName by rememberSaveable { mutableStateOf(NavTab.HOME.name) }
    var previousRouteName by rememberSaveable { mutableStateOf(NavTab.HOME.name) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var rulesMenuOpen by remember { mutableStateOf(false) }

    val current = NavTab.valueOf(routeName)
    val previous = NavTab.valueOf(previousRouteName)
    val bottomTabs = NavTab.entries.filter { it.inBottomBar }
    val selectedTab = if (current == NavTab.TEST) previous else current

    fun notify(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }

    fun openSimulator() {
        previousRouteName = routeName
        routeName = NavTab.TEST.name
    }

    BackHandler(enabled = current == NavTab.TEST) {
        routeName = previousRouteName
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(CsvManager.export(db.getRules()).toByteArray())
                }
            }.onSuccess {
                notify("Lista exportada correctamente")
            }.onFailure {
                notify("No se pudo exportar el archivo")
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
            }.onSuccess { result ->
                notify("Importación: ${result.added} añadidas, ${result.skipped} omitidas")
                refreshKey++
            }.onFailure {
                notify("No se pudo leer el archivo")
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.surfaceElevated,
                    contentColor = colors.textPrimary,
                    actionColor = colors.primary,
                    shape = MaterialTheme.shapes.small
                )
            }
        },
        topBar = {
            if (current != NavTab.HOME) {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = current.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colors.textPrimary
                                )
                                if (current.subtitle != null) {
                                    Spacer(Modifier.height(1.dp))
                                    Text(
                                        text = current.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            if (current == NavTab.TEST) {
                                IconButton(onClick = { routeName = previousRouteName }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver",
                                        tint = colors.textPrimary
                                    )
                                }
                            }
                        },
                        actions = {
                            when (current) {
                                NavTab.RULES -> {
                                    Box {
                                        IconButton(onClick = { rulesMenuOpen = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Más opciones",
                                                tint = colors.textSecondary
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = rulesMenuOpen,
                                            onDismissRequest = { rulesMenuOpen = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Importar lista (CSV)") },
                                                leadingIcon = { Icon(Icons.Default.Add, null) },
                                                onClick = {
                                                    rulesMenuOpen = false
                                                    importLauncher.launch(arrayOf("*/*"))
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Exportar lista (CSV)") },
                                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                                onClick = {
                                                    rulesMenuOpen = false
                                                    exportLauncher.launch("bloqueos-filtro.csv")
                                                }
                                            )
                                        }
                                    }
                                }

                                NavTab.ACTIVITY -> {
                                    IconButton(onClick = { openSimulator() }) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Abrir simulador",
                                            tint = colors.primary
                                        )
                                    }
                                }

                                else -> Unit
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.background,
                            scrolledContainerColor = colors.background
                        )
                    )
                    HorizontalDivider(color = colors.border)
                }
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = colors.border)
                NavigationBar(
                    containerColor = colors.background,
                    tonalElevation = 0.dp
                ) {
                    bottomTabs.forEach { item ->
                        val selected = item == selectedTab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { routeName = item.name },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.textPrimary,
                                indicatorColor = colors.primary.copy(
                                    alpha = if (colors.isDark) 0.16f else 0.10f
                                ),
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Crossfade(
                targetState = current,
                animationSpec = if (motionEnabled) tween(180) else snap(),
                label = "screenTransition"
            ) { screen ->
                key(refreshKey, refreshSignal) {
                    when (screen) {
                        NavTab.HOME -> HomeScreen(
                            db = db,
                            roleActive = roleActive,
                            onActivate = onActivate,
                            onManageRules = { routeName = NavTab.RULES.name },
                            onOpenSimulator = { openSimulator() },
                            onOpenActivity = { routeName = NavTab.ACTIVITY.name },
                            onOpenSettings = { routeName = NavTab.SETTINGS.name }
                        )

                        NavTab.RULES -> RulesScreen(db = db)

                        NavTab.ACTIVITY -> LogScreen(
                            db = db,
                            onOpenSimulator = { openSimulator() }
                        )

                        NavTab.TEST -> TestScreen(
                            db = db,
                            roleActive = roleActive,
                            onActivate = onActivate,
                            onOpenActivity = { routeName = NavTab.ACTIVITY.name }
                        )

                        NavTab.SETTINGS -> SettingsScreen(
                            db = db,
                            roleActive = roleActive,
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                            onActivate = onActivate,
                            onManageRules = { routeName = NavTab.RULES.name },
                            onImportCsv = { importLauncher.launch(arrayOf("*/*")) },
                            onExportCsv = { exportLauncher.launch("bloqueos-filtro.csv") },
                            onOpenActivity = { routeName = NavTab.ACTIVITY.name },
                            onLogCleared = {
                                refreshKey++
                                notify("Historial borrado")
                            }
                        )
                    }
                }
            }
        }
    }
}