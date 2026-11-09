package com.example.callfilter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    db: FilterDbHelper,
    roleActive: Boolean,
    onActivate: () -> Unit,
    onManageRules: () -> Unit,
    onOpenSimulator: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val stats = remember { db.getStats() }
    val rules = remember { db.getRules() }
    val log = remember { db.getLog() }
    val activeRules = rules.count { it.enabled }
    val totalRules = rules.size
    val blockRate = if (stats.total == 0) null else stats.blocked.toFloat() / stats.total
    val needsSetup = !roleActive || activeRules == 0
    val hasSimulation = log.any { it.source == "TEST" }
    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val zone = remember { ZoneId.systemDefault() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { HomeHeader(onOpenSimulator, onOpenSettings) }

        item {
            ProtectionHero(
                roleActive = roleActive,
                activeRules = activeRules,
                totalRules = totalRules,
                onActivate = onActivate,
                onManageRules = onManageRules
            )
        }

        if (needsSetup) {
            item {
                SetupCard(
                    roleActive = roleActive,
                    activeRules = activeRules,
                    totalRules = totalRules,
                    hasSimulation = hasSimulation,
                    onActivate = onActivate,
                    onManageRules = onManageRules,
                    onOpenSimulator = onOpenSimulator
                )
            }
        }

        item {
            SectionHeader("Resumen")
        }
        item {
            SummaryCard(
                total = stats.total,
                blocked = stats.blocked,
                blockRate = blockRate
            )
        }

        item {
            SectionHeader(
                title = "Actividad reciente",
                actionLabel = if (log.isEmpty()) "Probar" else "Ver todo",
                onAction = if (log.isEmpty()) onOpenSimulator else onOpenActivity
            )
        }
        item {
            if (log.isEmpty()) {
                NoticeCard(
                    icon = Icons.Default.Notifications,
                    title = "Todavía no hay llamadas registradas",
                    text = "En cuanto el filtro revise una llamada real o hagas una prueba, aparecerá aquí.",
                    tone = NoticeTone.INFO,
                    actionLabel = "Abrir simulador",
                    onAction = onOpenSimulator
                )
            } else {
                RecentActivityCard(
                    entries = log.take(4),
                    timeFormat = timeFormat,
                    zone = zone,
                    onClick = onOpenActivity
                )
            }
        }

        if (stats.byRule.isNotEmpty()) {
            item { SectionHeader("Bloqueos más aplicados") }
            item { TopRulesCard(stats.byRule.take(4)) }
        }

        item { PrivacyFooter() }
    }
}

@Composable
private fun HomeHeader(
    onOpenSimulator: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandMark()
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "CallFilter",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Privacidad de llamadas",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderIconButton(
                icon = Icons.Default.PlayArrow,
                contentDescription = "Abrir simulador",
                tint = colors.primary,
                onClick = onOpenSimulator
            )
            HeaderIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Abrir ajustes",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = AppTheme.colors.textSecondary
) {
    val colors = AppTheme.colors
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(MaterialTheme.shapes.small)
            .background(colors.surface)
            .border(1.dp, colors.border, MaterialTheme.shapes.small)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun ProtectionHero(
    roleActive: Boolean,
    activeRules: Int,
    totalRules: Int,
    onActivate: () -> Unit,
    onManageRules: () -> Unit
) {
    val colors = AppTheme.colors
    val fullyActive = roleActive && activeRules > 0
    val accent = when {
        fullyActive -> colors.success
        roleActive -> colors.warning
        else -> colors.warning
    }
    val statusText = when {
        fullyActive -> "Protección activa"
        roleActive -> "Sin reglas activas"
        else -> "Permiso pendiente"
    }
    val title = when {
        fullyActive -> "Llamadas bajo control"
        roleActive -> "Falta tu primera regla"
        else -> "Activa la protección"
    }
    val description = when {
        fullyActive -> if (activeRules == 1) {
            "Cada llamada entrante se compara con tu regla activa antes de sonar."
        } else {
            "Cada llamada entrante se compara con tus $activeRules reglas activas antes de sonar."
        }

        roleActive -> {
            "El permiso del sistema ya está concedido. Añade al menos un bloqueo para que el filtro actúe."
        }

        totalRules > 0 -> {
            "Tus $totalRules reglas están listas. Solo falta el permiso de filtrado de llamadas de Android."
        }

        else -> {
            "Concede el permiso de filtrado de llamadas para que CallFilter pueda revisar las llamadas entrantes."
        }
    }
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .padding(20.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(
                    text = statusText,
                    container = accent.copy(alpha = if (colors.isDark) 0.16f else 0.10f),
                    content = accent,
                    showDot = true
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "En el dispositivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShieldMark(
                    modifier = Modifier.size(34.dp),
                    tint = accent,
                    checkTint = colors.surface,
                    filled = fullyActive
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Spacer(Modifier.height(18.dp))
            when {
                !roleActive -> {
                    Button(
                        onClick = onActivate,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        contentPadding = PaddingValues(vertical = 13.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Activar protección", style = MaterialTheme.typography.labelLarge)
                    }
                }

                activeRules == 0 -> {
                    Button(
                        onClick = onManageRules,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        contentPadding = PaddingValues(vertical = 13.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir un bloqueo", style = MaterialTheme.typography.labelLarge)
                    }
                }

                else -> {
                    OutlinedButton(
                        onClick = onManageRules,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            colors.borderStrong
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(
                            "Gestionar listas",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    total: Int,
    blocked: Int,
    blockRate: Float?
) {
    val colors = AppTheme.colors
    AppCard {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Metric(
                value = total.toString(),
                label = "Llamadas revisadas",
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(42.dp),
                color = colors.border
            )
            Metric(
                value = blocked.toString(),
                label = "Bloqueadas",
                modifier = Modifier.weight(1f),
                valueColor = colors.danger
            )
            VerticalDivider(
                modifier = Modifier.height(42.dp),
                color = colors.border
            )
            Metric(
                value = blockRate?.let { "${(it * 100).roundToInt()}%" } ?: "—",
                label = "Tasa de bloqueo",
                modifier = Modifier.weight(1f),
                valueColor = colors.textPrimary
            )
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = colors.border)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "Solo llamadas reales. Las pruebas no alteran estas cifras.",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun SetupCard(
    roleActive: Boolean,
    activeRules: Int,
    totalRules: Int,
    hasSimulation: Boolean,
    onActivate: () -> Unit,
    onManageRules: () -> Unit,
    onOpenSimulator: () -> Unit
) {
    val colors = AppTheme.colors
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = Icons.Default.Warning,
                container = colors.warningContainer,
                content = colors.warning,
                size = 38.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Ponlo en marcha en 3 pasos",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    "Solo necesitas un momento para dejar el filtro listo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        StepRow(
            number = 1,
            title = "Activa la protección",
            text = if (roleActive) "Permiso concedido." else "Permite que Android revise las llamadas.",
            done = roleActive,
            actionLabel = "Activar",
            onAction = onActivate
        )
        StepRow(
            number = 2,
            title = "Añade bloqueos",
            text = when {
                totalRules == 0 -> "Crea tu primer prefijo o número bloqueado."
                activeRules == 0 -> "Tienes $totalRules reglas, pero ninguna está activa."
                else -> "Tienes $activeRules bloqueos activos."
            },
            done = activeRules > 0,
            actionLabel = "Ver",
            onAction = onManageRules
        )
        StepRow(
            number = 3,
            title = "Comprueba el resultado",
            text = if (hasSimulation) "Ya has probado una llamada de ejemplo."
            else "Simula una llamada antes de confiar en el filtro.",
            done = hasSimulation,
            actionLabel = "Probar",
            onAction = onOpenSimulator
        )
    }
}

@Composable
private fun RecentActivityCard(
    entries: List<LogEntry>,
    timeFormat: DateTimeFormatter,
    zone: ZoneId,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    AppCard(contentPadding = PaddingValues(0.dp), onClick = onClick) {
        entries.forEachIndexed { index, entry ->
            val blocked = entry.blocked
            val container = if (blocked) colors.dangerContainer else colors.successContainer
            val content = if (blocked) colors.danger else colors.success
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(
                    icon = if (blocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                    container = container,
                    content = content,
                    size = 38.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = entry.number,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry.rule?.let { friendlyRule(it) } ?: "Sin coincidencia",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    StatusPill(
                        text = if (blocked) "Bloqueada" else "Permitida",
                        container = container,
                        content = content
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = Instant.ofEpochMilli(entry.ts).atZone(zone).format(timeFormat) +
                                if (entry.source == "TEST") " · Prueba" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }
            if (index < entries.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 66.dp),
                    color = colors.border
                )
            }
        }
    }
}

@Composable
private fun TopRulesCard(byRule: List<Pair<String, Int>>) {
    val colors = AppTheme.colors
    val max = byRule.maxOf { it.second }.coerceAtLeast(1)
    AppCard {
        byRule.forEachIndexed { index, (rule, count) ->
            if (index > 0) Spacer(Modifier.height(15.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = friendlyRule(rule),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.danger,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(7.dp))
            LinearProgressIndicator(
                progress = { count.toFloat() / max },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = colors.danger,
                trackColor = colors.surfaceSunken
            )
        }
    }
}

@Composable
private fun PrivacyFooter() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShieldMark(
            modifier = Modifier.size(16.dp),
            tint = colors.textSecondary,
            checkTint = colors.background,
            filled = false
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Procesado en tu dispositivo · Sin servidores externos",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}