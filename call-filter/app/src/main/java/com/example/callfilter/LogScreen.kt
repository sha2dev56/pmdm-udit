package com.example.callfilter

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class LogFilter(val label: String) {
    ALL("Todas"),
    BLOCKED("Bloqueadas"),
    ALLOWED("Permitidas"),
    TEST("Pruebas")
}

@Composable
fun LogScreen(
    db: FilterDbHelper,
    onOpenSimulator: () -> Unit
) {
    val colors = AppTheme.colors
    var log by remember { mutableStateOf(db.getLog()) }
    var filter by remember { mutableStateOf(LogFilter.ALL) }
    var confirmClear by remember { mutableStateOf(false) }

    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()) }
    val zone = remember { ZoneId.systemDefault() }
    val today = LocalDate.now()

    val blockedCount = log.count { it.blocked }
    val allowedCount = log.size - blockedCount
    val filtered = when (filter) {
        LogFilter.ALL -> log
        LogFilter.BLOCKED -> log.filter { it.blocked }
        LogFilter.ALLOWED -> log.filter { !it.blocked }
        LogFilter.TEST -> log.filter { it.source == "TEST" }
    }
    val groups = filtered.groupBy { entry ->
        val date = Instant.ofEpochMilli(entry.ts).atZone(zone).toLocalDate()
        when (date) {
            today -> "Hoy"
            today.minusDays(1) -> "Ayer"
            else -> date.format(dateFormat)
        }
    }

    if (log.isEmpty()) {
        Box(Modifier.fillMaxSize()) {
            EmptyState(
                icon = Icons.Default.Notifications,
                title = "Sin actividad todavía",
                text = "Aquí aparecerán las llamadas revisadas por el filtro, tanto reales como simuladas.",
                actionLabel = "Probar el filtro",
                onAction = onOpenSimulator
            )
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            LogSummaryCard(
                total = log.size,
                blocked = blockedCount,
                allowed = allowedCount
            )
            Spacer(Modifier.height(12.dp))
            SegmentedPills(
                options = LogFilter.entries.map { SegmentOption(it.label) },
                selectedIndex = filter.ordinal,
                onSelect = { filter = LogFilter.entries[it] },
                compact = true
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filtered.size} de ${log.size} registros",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { log = db.getLog() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar actividad",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(2.dp))
                IconButton(
                    onClick = { confirmClear = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar historial",
                        tint = colors.danger,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Box(Modifier.weight(1f)) {
            if (filtered.isEmpty()) {
                EmptyState(
                    icon = if (filter == LogFilter.TEST) Icons.Default.Notifications else Icons.Default.Search,
                    title = when (filter) {
                        LogFilter.ALL -> "Nada que mostrar"
                        LogFilter.BLOCKED -> "Sin llamadas bloqueadas"
                        LogFilter.ALLOWED -> "Sin llamadas permitidas"
                        LogFilter.TEST -> "Sin pruebas guardadas"
                    },
                    text = when (filter) {
                        LogFilter.ALL -> "Todavía no hay llamadas en el registro."
                        LogFilter.BLOCKED -> "Ninguna llamada ha coincidido con tus bloqueos."
                        LogFilter.ALLOWED -> "Todas las llamadas del registro han sido bloqueadas."
                        LogFilter.TEST -> "Las simulaciones que hagas aparecerán aquí."
                    },
                    actionLabel = if (filter == LogFilter.TEST) "Abrir simulador" else "Ver todas",
                    onAction = {
                        if (filter == LogFilter.TEST) onOpenSimulator() else filter = LogFilter.ALL
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groups.forEach { (day, entries) ->
                        item(key = "day-$day") {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, top = 8.dp, bottom = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.primary
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = "${entries.size} ${if (entries.size == 1) "registro" else "registros"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        item(key = "entries-$day") {
                            DayCard(
                                entries = entries,
                                timeFormat = timeFormat,
                                zone = zone
                            )
                        }
                    }
                    if (log.size >= 200) {
                        item {
                            Text(
                                text = "Mostrando las 200 llamadas más recientes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "¿Borrar el historial?",
            message = "Se eliminarán todas las llamadas registradas y las estadísticas asociadas. Esta acción no se puede deshacer.",
            confirmLabel = "Borrar",
            onConfirm = {
                db.clearLog()
                log = db.getLog()
                confirmClear = false
            },
            onDismiss = { confirmClear = false }
        )
    }
}

@Composable
private fun LogSummaryCard(
    total: Int,
    blocked: Int,
    allowed: Int
) {
    val colors = AppTheme.colors
    AppCard {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                value = allowed.toString(),
                label = "Permitidas",
                modifier = Modifier.weight(1f),
                valueColor = colors.success
            )
            VerticalDivider(
                modifier = Modifier.height(42.dp),
                color = colors.border
            )
            Metric(
                value = total.toString(),
                label = "Registros",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = colors.border)
        Spacer(Modifier.height(9.dp))
        Text(
            text = "Incluye llamadas reales y pruebas del simulador.",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun DayCard(
    entries: List<LogEntry>,
    timeFormat: DateTimeFormatter,
    zone: ZoneId
) {
    val colors = AppTheme.colors
    AppCard(contentPadding = PaddingValues(0.dp)) {
        entries.forEachIndexed { index, entry ->
            val blocked = entry.blocked
            val container = if (blocked) colors.dangerContainer else colors.successContainer
            val content = if (blocked) colors.danger else colors.success
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(
                    icon = if (blocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                    container = container,
                    content = content,
                    size = 40.dp
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
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = entry.rule?.let { friendlyRule(it) } ?: "Sin coincidencia",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Instant.ofEpochMilli(entry.ts).atZone(zone).format(timeFormat),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(colors.borderStrong)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (entry.source == "TEST") "Simulada" else "Real",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.source == "TEST") colors.secondary else colors.textSecondary
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                StatusPill(
                    text = if (blocked) "Bloqueada" else "Permitida",
                    container = container,
                    content = content
                )
            }
            if (index < entries.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 67.dp),
                    color = colors.border
                )
            }
        }
    }
}
