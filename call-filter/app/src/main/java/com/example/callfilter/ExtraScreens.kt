package com.example.callfilter

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- Utilidades de texto e iconos ----------

fun friendlyRule(desc: String): String {
    val parts = desc.split(":", limit = 2)
    if (parts.size < 2) return desc
    return when (parts[0]) {
        "PREFIX" -> "Prefijo ${parts[1]}"
        "NUMBER" -> "Número ${parts[1]}"
        "WHITELIST" -> "Lista blanca ${parts[1]}"
        else -> desc
    }
}

private fun typeIcon(t: RuleType): ImageVector = when (t) {
    RuleType.PREFIX -> Icons.Default.Call
    RuleType.NUMBER -> Icons.Default.Person
    RuleType.WHITELIST -> Icons.Default.CheckCircle
}

private fun typeName(t: RuleType) = when (t) {
    RuleType.PREFIX -> "Prefijos"
    RuleType.NUMBER -> "Números"
    RuleType.WHITELIST -> "Lista blanca"
}

private fun typeHelp(t: RuleType) = when (t) {
    RuleType.PREFIX -> "Bloquea todas las llamadas cuyo número empiece por estos dígitos (por ejemplo 400)."
    RuleType.NUMBER -> "Bloquea números concretos."
    RuleType.WHITELIST -> "Estos números nunca se bloquean, salvo que una regla de bloqueo tenga más prioridad."
}

// ---------- Componentes comunes ----------

@Composable
private fun IconBadge(icon: ImageVector, container: Color, content: Color) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(container),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = content) }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, text: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ---------- Inicio ----------

@Composable
fun HomeScreen(db: FilterDbHelper, roleActive: Boolean, onActivate: () -> Unit) {
    val stats = remember { db.getStats() }
    val activeRules = remember { db.getActiveRules().size }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StatusCard(roleActive, onActivate) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Llamadas", stats.total.toString(), Modifier.weight(1f))
                StatCard("Bloqueadas", stats.blocked.toString(), Modifier.weight(1f))
                StatCard("Reglas activas", activeRules.toString(), Modifier.weight(1f))
            }
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Reglas aplicadas", style = MaterialTheme.typography.titleMedium)
                    if (stats.byRule.isEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Todavía no se ha aplicado ninguna regla a llamadas reales.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        stats.byRule.take(5).forEach { (rule, n) ->
                            Row(
                                Modifier.fillMaxWidth().padding(top = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(friendlyRule(rule))
                                Text("$n", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Las estadísticas solo cuentan llamadas reales; las simuladas se ven en el registro.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusCard(active: Boolean, onActivate: () -> Unit) {
    val container = if (active) Semantic.successContainer() else MaterialTheme.colorScheme.errorContainer
    val content = if (active) Semantic.onSuccessContainer() else MaterialTheme.colorScheme.onErrorContainer
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (active) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (active) "Protección activa" else "Filtro desactivado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (active) "Las llamadas entrantes se revisan con tus reglas."
                    else "Concede el permiso para poder bloquear llamadas."
                )
                if (!active) {
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onActivate) { Text("Activar ahora") }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------- Reglas ----------

@Composable
fun RulesScreen(db: FilterDbHelper) {
    val context = LocalContext.current
    var all by remember { mutableStateOf(db.getRules()) }
    var type by remember { mutableStateOf(RuleType.PREFIX) }
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Rule?>(null) }
    var toDelete by remember { mutableStateOf<Rule?>(null) }

    fun refresh() { all = db.getRules() }
    val rules = all.filter { it.type == type }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RuleType.entries.forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text(typeName(t)) },
                        leadingIcon = if (type == t) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
            Text(
                typeHelp(type),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (rules.isEmpty()) {
                EmptyState(
                    typeIcon(type),
                    "Aún no hay ${typeName(type).lowercase()}",
                    "Pulsa «Añadir» para crear la primera."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rules, key = { it.id }) { r ->
                        RuleCard(
                            rule = r,
                            onToggle = { db.setRuleEnabled(r.id, it); refresh() },
                            onEdit = { editing = r; showDialog = true },
                            onDelete = { toDelete = r }
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { editing = null; showDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Añadir") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    if (showDialog) {
        val current = editing
        RuleDialog(
            type = type,
            initial = current,
            onDismiss = { showDialog = false },
            onSave = { input, prio, start, end ->
                val v = CallFilter.normalize(input)
                if (v.isEmpty()) {
                    Toast.makeText(context, "Escribe un valor", Toast.LENGTH_SHORT).show()
                } else {
                    val p = prio ?: type.defaultPriority
                    val ok = if (current == null) db.insertRule(type, v, p, start, end) != -1L
                    else db.updateRule(current.id, v, p, start, end) > 0
                    if (!ok) Toast.makeText(context, "Esa regla ya existe", Toast.LENGTH_SHORT).show()
                    showDialog = false
                    refresh()
                }
            }
        )
    }

    toDelete?.let { r ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("¿Eliminar regla?") },
            text = { Text("Se eliminará ${r.value}. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { db.deleteRule(r.id); toDelete = null; refresh() }) {
                    Text("Eliminar")
                }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
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
    var menu by remember { mutableStateOf(false) }
    val blocking = rule.type != RuleType.WHITELIST
    val container = if (blocking) MaterialTheme.colorScheme.errorContainer else Semantic.successContainer()
    val content = if (blocking) MaterialTheme.colorScheme.onErrorContainer else Semantic.onSuccessContainer()
    val franja = if (rule.startMin >= 0 && rule.endMin >= 0)
        " · ${fmtHm(rule.startMin)}–${fmtHm(rule.endMin)}" else ""

    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(typeIcon(rule.type), container, content)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (rule.type == RuleType.PREFIX) "${rule.value}…" else rule.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (rule.enabled) Color.Unspecified else MaterialTheme.colorScheme.outline
                )
                Text(
                    "Prioridad ${rule.priority}$franja",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = rule.enabled, onCheckedChange = onToggle)
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { menu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                        onClick = { menu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleDialog(
    type: RuleType,
    initial: Rule?,
    onDismiss: () -> Unit,
    onSave: (String, Int?, Int, Int) -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf(initial?.value ?: "") }
    var prio by remember { mutableStateOf((initial?.priority ?: type.defaultPriority).toString()) }
    var useWindow by remember { mutableStateOf(initial != null && initial.startMin >= 0) }
    var from by remember {
        mutableStateOf(initial?.takeIf { it.startMin >= 0 }?.let { fmtHm(it.startMin) } ?: "")
    }
    var to by remember {
        mutableStateOf(initial?.takeIf { it.endMin >= 0 }?.let { fmtHm(it.endMin) } ?: "")
    }

    val label = when (type) {
        RuleType.PREFIX -> "Prefijo"
        RuleType.NUMBER -> "Número a bloquear"
        RuleType.WHITELIST -> "Número permitido"
    }
    val hint = when (type) {
        RuleType.PREFIX -> "Ej. 400"
        else -> "Ej. 612345678"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial == null) "Nueva regla · ${typeName(type)}" else "Editar regla")
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(label) },
                    placeholder = { Text(hint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = prio,
                    onValueChange = { prio = it.filter { c -> c.isDigit() } },
                    label = { Text("Prioridad") },
                    supportingText = { Text("Si dos reglas coinciden, gana la de mayor prioridad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Limitar a una franja horaria", Modifier.weight(1f))
                    Switch(checked = useWindow, onCheckedChange = { useWindow = it })
                }
                if (useWindow) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = from,
                            onValueChange = { from = it },
                            label = { Text("Desde") },
                            placeholder = { Text("22:00") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = to,
                            onValueChange = { to = it },
                            label = { Text("Hasta") },
                            placeholder = { Text("08:00") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (useWindow) {
                    val s = parseHm(from)
                    val e = parseHm(to)
                    if (s == null || e == null || s < 0 || e < 0) {
                        Toast.makeText(
                            context, "Franja no válida. Usa el formato HH:mm", Toast.LENGTH_LONG
                        ).show()
                    } else {
                        onSave(text, prio.toIntOrNull(), s, e)
                    }
                } else {
                    onSave(text, prio.toIntOrNull(), -1, -1)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ---------- Registro ----------

@Composable
fun LogScreen(db: FilterDbHelper) {
    var log by remember { mutableStateOf(db.getLog()) }
    var confirmClear by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("dd MMM · HH:mm:ss", Locale.getDefault()) }

    if (log.isEmpty()) {
        EmptyState(
            Icons.Default.DateRange,
            "Sin actividad",
            "Aquí aparecerán las llamadas revisadas, reales y simuladas."
        )
    } else {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${log.size} registros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    IconButton(onClick = { log = db.getLog() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                    TextButton(onClick = { confirmClear = true }) { Text("Borrar") }
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(log, key = { it.id }) { e ->
                    val container =
                        if (e.blocked) MaterialTheme.colorScheme.errorContainer else Semantic.successContainer()
                    val content =
                        if (e.blocked) MaterialTheme.colorScheme.onErrorContainer else Semantic.onSuccessContainer()
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconBadge(
                                if (e.blocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                                container,
                                content
                            )
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    e.number,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    (if (e.blocked) "Bloqueada" else "Permitida") +
                                            (e.rule?.let { " · ${friendlyRule(it)}" } ?: ""),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "${fmt.format(Date(e.ts))} · " +
                                            (if (e.source == "TEST") "Simulada" else "Real"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("¿Borrar el registro?") },
            text = { Text("Se eliminarán todas las llamadas registradas y las estadísticas.") },
            confirmButton = {
                TextButton(onClick = { db.clearLog(); log = db.getLog(); confirmClear = false }) {
                    Text("Borrar")
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancelar") } }
        )
    }
}

// ---------- Simulador ----------

private data class SimResult(val blocked: Boolean, val rule: String?)

@Composable
fun TestScreen(db: FilterDbHelper) {
    var number by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<SimResult?>(null) }

    fun simulate(n: String?) {
        val parsed = parseHm(hour)
        if (parsed == null) {
            error = "Hora no válida. Usa el formato HH:mm"
            return
        }
        error = null
        val now = if (parsed < 0) currentMinutes() else parsed
        val d = FilterEngine.decide(n, db.getActiveRules(), now)
        val r = d.rule
        val desc = r?.let { "${it.type}:${it.value}" }
        result = SimResult(
            d.blocked,
            r?.let { "${friendlyRule("${it.type}:${it.value}")} · prioridad ${it.priority}" }
        )
        db.logCall(CallFilter.mask(n), d.blocked, desc, "TEST")
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Simula una llamada entrante", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Comprueba qué haría el filtro sin recibir una llamada real.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Número entrante") },
                    placeholder = { Text("+34400123456") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text("Hora simulada (opcional)") },
                    placeholder = { Text("23:00") },
                    supportingText = { Text(error ?: "Vacío = hora actual") },
                    isError = error != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { simulate(number) },
                        enabled = number.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) { Text("Simular") }
                    OutlinedButton(onClick = { simulate(null) }, modifier = Modifier.weight(1f)) {
                        Text("Número oculto")
                    }
                }
            }
        }

        result?.let { r ->
            val container =
                if (r.blocked) MaterialTheme.colorScheme.errorContainer else Semantic.successContainer()
            val content =
                if (r.blocked) MaterialTheme.colorScheme.onErrorContainer else Semantic.onSuccessContainer()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = container, contentColor = content)
            ) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (r.blocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (r.blocked) "Llamada bloqueada" else "Llamada permitida",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            r.rule ?: "Ninguna regla coincide (comportamiento por defecto)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}