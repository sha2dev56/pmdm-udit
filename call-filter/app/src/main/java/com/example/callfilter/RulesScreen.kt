package com.example.callfilter

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(db: FilterDbHelper) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    var all by remember { mutableStateOf(db.getRules()) }
    var type by remember { mutableStateOf(RuleType.PREFIX) }
    var query by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Rule?>(null) }
    var toDelete by remember { mutableStateOf<Rule?>(null) }

    fun refresh() {
        all = db.getRules()
    }

    val rules = all.filter { it.type == type }
    val activeInType = rules.count { it.enabled }
    val trimmedQuery = query.trim()
    val normalizedQuery = trimmedQuery.filter { it.isDigit() || it == '+' }
    val searched = if (trimmedQuery.isEmpty()) {
        rules
    } else {
        rules.filter { rule ->
            if (normalizedQuery.isNotEmpty()) {
                rule.value.contains(normalizedQuery)
            } else {
                rule.value.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Buscar número o prefijo"
                )
                Spacer(Modifier.height(12.dp))
                SegmentedPills(
                    options = RuleType.entries.map {
                        SegmentOption(typeName(it), typeIcon(it))
                    },
                    selectedIndex = type.ordinal,
                    onSelect = { index ->
                        type = RuleType.entries[index]
                        query = ""
                    }
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(14.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = typeHelp(type),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (trimmedQuery.isEmpty()) {
                            "${rules.size} ${if (rules.size == 1) "entrada" else "entradas"}"
                        } else {
                            "${searched.size} ${if (searched.size == 1) "coincidencia" else "coincidencias"}"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "· $activeInType ${if (activeInType == 1) "activa" else "activas"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(Modifier.weight(1f)) {
                when {
                    rules.isEmpty() -> {
                        EmptyState(
                            icon = typeIcon(type),
                            title = when (type) {
                                RuleType.PREFIX -> "Sin prefijos bloqueados"
                                RuleType.NUMBER -> "Sin números bloqueados"
                                RuleType.WHITELIST -> "Sin números permitidos"
                            },
                            text = when (type) {
                                RuleType.PREFIX -> "Bloquea todas las llamadas que empiecen por un prefijo."
                                RuleType.NUMBER -> "Bloquea un número concreto, uno por regla."
                                RuleType.WHITELIST -> "Añade un número para que nunca se bloquee."
                            },
                            actionLabel = typeAddLabel(type),
                            onAction = {
                                editing = null
                                showDialog = true
                            }
                        )
                    }

                    searched.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = "Sin resultados",
                            text = "No hay entradas en esta lista que coincidan con «$trimmedQuery».",
                            actionLabel = "Limpiar búsqueda",
                            onAction = { query = "" }
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                top = 2.dp,
                                end = 20.dp,
                                bottom = 104.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(searched, key = { it.id }) { rule ->
                                RuleCard(
                                    rule = rule,
                                    onToggle = {
                                        db.setRuleEnabled(rule.id, it)
                                        refresh()
                                    },
                                    onEdit = {
                                        editing = rule
                                        showDialog = true
                                    },
                                    onDelete = { toDelete = rule }
                                )
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                editing = null
                showDialog = true
            },
            icon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            text = {
                Text(typeAddLabel(type), style = MaterialTheme.typography.labelLarge)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = MaterialTheme.shapes.large,
            containerColor = colors.primary,
            contentColor = colors.onPrimary
        )
    }

    if (showDialog) {
        val current = editing
        RuleEditorSheet(
            type = type,
            initial = current,
            onDismiss = { showDialog = false },
            onSave = { value, priority, start, end ->
                val saved = if (current == null) {
                    db.insertRule(type, value, priority ?: type.defaultPriority, start, end) != -1L
                } else {
                    db.updateRule(current.id, value, priority, start, end) > 0
                }
                if (saved) {
                    showDialog = false
                    refresh()
                }
                saved
            }
        )
    }

    toDelete?.let { rule ->
        ConfirmDialog(
            title = "¿Quitar de la lista?",
            message = "Se eliminará «${rule.value}» de tus listas. Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            onConfirm = {
                db.deleteRule(rule.id)
                toDelete = null
                refresh()
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun RuleCard(
    rule: Rule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    var menu by remember { mutableStateOf(false) }
    val allows = rule.type == RuleType.WHITELIST
    val badgeContainer = if (allows) colors.successContainer else colors.dangerContainer
    val badgeContent = if (allows) colors.success else colors.danger
    val statusContainer = when {
        !rule.enabled -> colors.surfaceSunken
        allows -> colors.successContainer
        else -> colors.dangerContainer
    }
    val statusContent = when {
        !rule.enabled -> colors.textSecondary
        allows -> colors.success
        else -> colors.danger
    }
    val statusText = when {
        !rule.enabled -> "Pausada"
        allows -> "Permite"
        else -> "Bloquea"
    }
    val schedule = if (rule.startMin >= 0 && rule.endMin >= 0) {
        " · ${fmtHm(rule.startMin)}–${fmtHm(rule.endMin)}"
    } else {
        ""
    }

    AppCard(
        contentPadding = PaddingValues(0.dp),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = typeIcon(rule.type),
                container = badgeContainer,
                content = badgeContent,
                size = 42.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (rule.type == RuleType.PREFIX) "${rule.value}…" else rule.value,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (rule.enabled) colors.textPrimary else colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(7.dp))
                    StatusPill(
                        text = statusText,
                        container = statusContainer,
                        content = statusContent
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${typeLabel(rule.type)} · ${priorityLabel(rule.priority)}$schedule",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(
                checked = rule.enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(start = 4.dp)
            )
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones de la entrada",
                        tint = colors.textSecondary
                    )
                }
                DropdownMenu(
                    expanded = menu,
                    onDismissRequest = { menu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = {
                            menu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = colors.danger) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = colors.danger)
                        },
                        onClick = {
                            menu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuleEditorSheet(
    type: RuleType,
    initial: Rule?,
    onDismiss: () -> Unit,
    onSave: (String, Int?, Int, Int) -> Boolean
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf(initial?.value.orEmpty()) }
    var priority by remember {
        mutableStateOf((initial?.priority ?: type.defaultPriority).toString())
    }
    var useWindow by remember {
        mutableStateOf(initial != null && initial.startMin >= 0 && initial.endMin >= 0)
    }
    var from by remember {
        mutableStateOf(initial?.takeIf { it.startMin >= 0 }?.let { fmtHm(it.startMin) }.orEmpty())
    }
    var to by remember {
        mutableStateOf(initial?.takeIf { it.endMin >= 0 }?.let { fmtHm(it.endMin) }.orEmpty())
    }
    var advanced by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val label = when (type) {
        RuleType.PREFIX -> "Prefijo a bloquear"
        RuleType.NUMBER -> "Número a bloquear"
        RuleType.WHITELIST -> "Número permitido"
    }
    val hint = when (type) {
        RuleType.PREFIX -> "Ej. 400"
        else -> "Ej. 612345678"
    }
    val supporting = when (type) {
        RuleType.PREFIX -> "Se ignoran espacios, guiones y el prefijo +34."
        else -> "Solo se comparan los dígitos del número."
    }
    val title = when {
        initial != null -> "Editar ${typeLabel(type).lowercase()}"
        type == RuleType.WHITELIST -> "Nuevo número permitido"
        type == RuleType.PREFIX -> "Nuevo prefijo bloqueado"
        else -> "Nuevo número bloqueado"
    }

    fun save() {
        val normalized = CallFilter.normalize(text)
        if (normalized.isEmpty()) {
            error = "Escribe un número o prefijo válido."
            return
        }
        val start: Int
        val end: Int
        if (useWindow) {
            val s = parseHm(from)
            val e = parseHm(to)
            if (s == null || e == null || s < 0 || e < 0) {
                error = "Indica una hora de inicio y de fin válidas."
                return
            }
            if (s == e) {
                error = "La hora de inicio y la de fin deben ser distintas."
                return
            }
            start = s
            end = e
        } else {
            start = -1
            end = -1
        }
        error = null
        val saved = onSave(
            normalized,
            priority.toIntOrNull()?.coerceIn(0, 999) ?: type.defaultPriority,
            start,
            end
        )
        if (!saved) error = "Ya existe una entrada con ese valor en esta lista."
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceElevated,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            androidx.compose.material3.BottomSheetDefaults.DragHandle(color = colors.borderStrong)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = typeIcon(type),
                    container = if (type == RuleType.WHITELIST) colors.successContainer
                    else colors.dangerContainer,
                    content = if (type == RuleType.WHITELIST) colors.success else colors.danger,
                    size = 44.dp
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = typeName(type),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    error = null
                },
                label = { Text(label) },
                placeholder = { Text(hint) },
                supportingText = { Text(supporting) },
                isError = error != null && text.isEmpty(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(colors.surfaceSunken)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Limitar por horario",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        "La regla solo se aplica dentro de esa franja.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Switch(
                    checked = useWindow,
                    onCheckedChange = {
                        useWindow = it
                        error = null
                        if (it) {
                            if (from.isBlank()) from = fmtHm(22 * 60)
                            if (to.isBlank()) to = fmtHm(8 * 60)
                        }
                    }
                )
            }

            if (useWindow) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimeField(
                        label = "Desde",
                        value = from,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            pickTime(context, from) {
                                from = it
                                error = null
                            }
                        }
                    )
                    TimeField(
                        label = "Hasta",
                        value = to,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            pickTime(context, to) {
                                to = it
                                error = null
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { advanced = !advanced },
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (advanced) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text("Opciones avanzadas", style = MaterialTheme.typography.labelLarge)
            }

            if (advanced) {
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = priority,
                    onValueChange = { input ->
                        priority = input.filter { it.isDigit() }.take(3)
                    },
                    label = { Text("Prioridad") },
                    supportingText = {
                        Text("Si dos reglas coinciden, se aplica la de mayor prioridad.")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = colors.danger,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.danger
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { save() },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 26.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (initial == null) "Añadir" else "Guardar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, colors.border),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value.ifBlank { "--:--" },
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary
            )
        }
    }
}

private fun pickTime(context: Context, current: String, onPicked: (String) -> Unit) {
    val minutes = current.let { parseHm(it) }?.takeIf { it >= 0 } ?: currentMinutes()
    TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(fmtHm(hour * 60 + minute)) },
        minutes / 60,
        minutes % 60,
        true
    ).show()
}
