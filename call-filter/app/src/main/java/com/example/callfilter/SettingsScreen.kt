package com.example.callfilter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    db: FilterDbHelper,
    roleActive: Boolean,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onActivate: () -> Unit,
    onManageRules: () -> Unit,
    onImportCsv: () -> Unit,
    onExportCsv: () -> Unit,
    onOpenActivity: () -> Unit,
    onLogCleared: () -> Unit
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val rules = remember { db.getRules() }
    val activeRules = rules.count { it.enabled }
    val logCount = remember { db.getLogCount() }
    var confirmClear by remember { mutableStateOf(false) }
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            Column {
                SectionHeader("Apariencia")
                Spacer(Modifier.height(10.dp))
                AppearanceCard(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange
                )
            }
        }

        item {
            Column {
                SectionHeader("Protección")
                Spacer(Modifier.height(10.dp))
                SettingsGroup {
                    SettingRow(
                        icon = Icons.Default.Lock,
                        title = "Permiso de filtrado",
                        subtitle = if (roleActive) {
                            "Concedido. Android permite que CallFilter revise las llamadas entrantes."
                        } else {
                            "Pendiente. Sin este permiso del sistema, el filtro no puede actuar."
                        },
                        trailing = {
                            StatusPill(
                                text = if (roleActive) "Concedido" else "Pendiente",
                                container = if (roleActive) colors.successContainer else colors.warningContainer,
                                content = if (roleActive) colors.success else colors.warning,
                                showDot = true
                            )
                        }
                    )
                    if (!roleActive) {
                        SettingDivider()
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                text = "El permiso se gestiona desde Android y solo lo usan las apps de filtrado de llamadas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onActivate,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.onPrimary
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text(
                                    "Activar protección",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader("Listas y datos")
                Spacer(Modifier.height(10.dp))
                SettingsGroup {
                    SettingRow(
                        icon = Icons.Default.Lock,
                        title = "Listas de bloqueo",
                        subtitle = "$activeRules activas de ${rules.size} entradas",
                        onClick = onManageRules
                    )
                    SettingDivider()
                    SettingRow(
                        icon = Icons.Default.Add,
                        title = "Importar lista CSV",
                        subtitle = "Añade reglas desde un archivo compatible.",
                        onClick = onImportCsv
                    )
                    SettingDivider()
                    SettingRow(
                        icon = Icons.Default.Share,
                        title = "Exportar lista CSV",
                        subtitle = "Guarda una copia de tus reglas.",
                        onClick = onExportCsv
                    )
                }
            }
        }

        item {
            Column {
                SectionHeader("Actividad")
                Spacer(Modifier.height(10.dp))
                SettingsGroup {
                    SettingRow(
                        icon = Icons.Default.Notifications,
                        title = "Historial de llamadas",
                        subtitle = if (logCount == 0) "Todavía no hay registros."
                        else "$logCount ${if (logCount == 1) "registro guardado" else "registros guardados"}",
                        onClick = onOpenActivity
                    )
                    SettingDivider()
                    SettingRow(
                        icon = Icons.Default.Delete,
                        title = "Borrar historial",
                        subtitle = "Elimina las llamadas registradas y sus estadísticas.",
                        danger = true,
                        onClick = { confirmClear = true }
                    )
                }
            }
        }

        item {
            Column {
                SectionHeader("Acerca de")
                Spacer(Modifier.height(10.dp))
                AboutCard(version = version)
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShieldMark(
                    modifier = Modifier.size(15.dp),
                    tint = colors.textSecondary,
                    checkTint = colors.background,
                    filled = false
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "Privacidad local por diseño",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "¿Borrar el historial?",
            message = "Se eliminarán todas las llamadas registradas y las estadísticas asociadas. Tus listas de bloqueo no se modificarán.",
            confirmLabel = "Borrar historial",
            onConfirm = {
                db.clearLog()
                confirmClear = false
                onLogCleared()
            },
            onDismiss = { confirmClear = false }
        )
    }
}

@Composable
private fun AppearanceCard(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val colors = AppTheme.colors
    AppCard {
        Text(
            text = "Tema de la aplicación",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = "Elige el aspecto que te resulte más cómodo. Los cambios se aplican al instante.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeMode.entries.forEach { mode ->
                ThemeOption(
                    mode = mode,
                    selected = mode == themeMode,
                    onClick = { onThemeModeChange(mode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private data class PreviewPalette(
    val background: Color,
    val surface: Color,
    val accent: Color,
    val text: Color
)

private val LightPreview = PreviewPalette(
    background = Color(0xFFF7F7F8),
    surface = Color(0xFFFFFFFF),
    accent = Color(0xFF4338CA),
    text = Color(0xFF6B7280)
)

private val DarkPreview = PreviewPalette(
    background = Color(0xFF0A0A0B),
    surface = Color(0xFF18181B),
    accent = Color(0xFF818CF8),
    text = Color(0xFFA1A1AA)
)

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val label = when (mode) {
        ThemeMode.SYSTEM -> "Sistema"
        ThemeMode.LIGHT -> "Claro"
        ThemeMode.DARK -> "Oscuro"
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.primary.copy(alpha = 0.08f) else colors.surfaceSunken)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) colors.primary else colors.border,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (mode == ThemeMode.LIGHT) LightPreview.background else DarkPreview.background)
        ) {
            if (mode == ThemeMode.SYSTEM) {
                Row(Modifier.fillMaxSize()) {
                    PreviewPane(
                        palette = LightPreview,
                        modifier = Modifier.weight(1f)
                    )
                    PreviewPane(
                        palette = DarkPreview,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                PreviewPane(
                    palette = if (mode == ThemeMode.LIGHT) LightPreview else DarkPreview,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) colors.primary else colors.textSecondary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
            if (selected) {
                Spacer(Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun PreviewPane(
    palette: PreviewPalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(palette.background)
            .padding(7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(palette.accent)
            )
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .width(19.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.text.copy(alpha = 0.75f))
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(13.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(palette.surface)
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth(0.7f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(palette.text.copy(alpha = 0.45f))
        )
    }
}

@Composable
private fun AboutCard(version: String) {
    val colors = AppTheme.colors
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark(size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "CallFilter",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = "Versión $version",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = colors.border)
        Spacer(Modifier.height(13.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(17.dp)
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = "CallFilter aplica tus reglas en el propio dispositivo. No envía números, historial ni estadísticas a servidores externos y no requiere conexión a internet.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(colors.primary.copy(alpha = if (colors.isDark) 0.10f else 0.06f))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Bloqueo local · Sin rastreo · Sin cuentas",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary
            )
        }
    }
}
