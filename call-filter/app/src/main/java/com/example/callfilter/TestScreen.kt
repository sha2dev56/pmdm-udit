package com.example.callfilter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private data class SimResult(
    val blocked: Boolean,
    val ruleLabel: String?,
    val priority: Int?,
    val minuteOfDay: Int
)

@Composable
fun TestScreen(
    db: FilterDbHelper,
    roleActive: Boolean,
    onActivate: () -> Unit,
    onOpenActivity: () -> Unit
) {
    val colors = AppTheme.colors
    val motionEnabled = rememberMotionEnabled()
    val now = remember { currentMinutes() }
    val activeRules = remember { db.getActiveRules() }
    var number by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<SimResult?>(null) }

    fun simulate(raw: String?) {
        val parsed = parseHm(time)
        if (parsed == null) {
            error = "Hora no válida. Usa el formato HH:mm."
            return
        }
        error = null
        val minute = if (parsed < 0) currentMinutes() else parsed
        val decision = FilterEngine.decide(raw, activeRules, minute)
        val rule = decision.rule
        result = SimResult(
            blocked = decision.blocked,
            ruleLabel = rule?.let { friendlyRule("${it.type}:${it.value}") },
            priority = rule?.priority,
            minuteOfDay = minute
        )
        db.logCall(
            CallFilter.mask(raw),
            decision.blocked,
            rule?.let { "${it.type}:${it.value}" },
            "TEST"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!roleActive) {
            NoticeCard(
                icon = Icons.Default.Warning,
                title = "El filtro real está desactivado",
                text = "Puedes probar tus reglas aquí, pero las llamadas reales no se filtrarán hasta conceder el permiso.",
                tone = NoticeTone.WARNING,
                actionLabel = "Activar protección",
                onAction = onActivate
            )
        }

        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Default.PlayArrow,
                    container = colors.primary.copy(alpha = if (colors.isDark) 0.14f else 0.08f),
                    content = colors.primary,
                    size = 42.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Llamada de prueba",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Comprueba qué decisión tomaría el filtro sin recibir una llamada real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = number,
                onValueChange = {
                    number = it
                    result = null
                },
                label = { Text("Número entrante") },
                placeholder = { Text("+34 612 345 678") },
                supportingText = { Text("Acepta espacios, guiones y el prefijo +34.") },
                trailingIcon = {
                    if (number.isNotEmpty()) {
                        IconButton(onClick = {
                            number = ""
                            result = null
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Borrar número",
                                tint = colors.textSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = time,
                onValueChange = {
                    time = it
                    result = null
                    error = null
                },
                label = { Text("Hora de la llamada (opcional)") },
                placeholder = { Text("HH:mm") },
                supportingText = {
                    Text(error ?: "Vacío = ahora mismo (${fmtHm(now)}).")
                },
                isError = error != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {
                        number = "400123456"
                        result = null
                    },
                    label = { Text("Prefijo 400") },
                    shape = MaterialTheme.shapes.small,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colors.surfaceSunken,
                        labelColor = colors.textPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                )
                AssistChip(
                    onClick = {
                        number = "612345678"
                        result = null
                    },
                    label = { Text("Móvil") },
                    shape = MaterialTheme.shapes.small,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colors.surfaceSunken,
                        labelColor = colors.textPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { simulate(number) },
                    enabled = number.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Simular", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = { simulate(null) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Número oculto", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (activeRules.isEmpty()) {
            NoticeCard(
                icon = Icons.Default.Info,
                title = "No hay reglas activas",
                text = "El filtro dejaría pasar todas las llamadas. Añade un bloqueo o activa una regla existente.",
                tone = NoticeTone.INFO
            )
        }

        AnimatedVisibility(
            visible = result != null,
            enter = if (motionEnabled) {
                fadeIn(tween(220)) + expandVertically(tween(220))
            } else {
                fadeIn(snap())
            },
            exit = if (motionEnabled) {
                fadeOut(tween(120)) + shrinkVertically(tween(160))
            } else {
                fadeOut(snap())
            }
        ) {
            result?.let { simulation ->
                SimulationResultCard(
                    result = simulation,
                    onOpenActivity = onOpenActivity
                )
            }
        }

        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = "Cómo se decide",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
            }
            Spacer(Modifier.height(14.dp))
            ExplanationStep(
                number = 1,
                text = "Se normaliza el número: se ignoran espacios, guiones y el prefijo +34."
            )
            ExplanationStep(
                number = 2,
                text = "Se buscan reglas activas que coincidan con el número y la hora elegida."
            )
            ExplanationStep(
                number = 3,
                text = "Gana la regla de mayor prioridad. Si no hay coincidencia, la llamada pasa."
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = colors.border)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Los números ocultos nunca se bloquean. CallFilter no consulta bases de datos externas: usa únicamente tus reglas.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        Text(
            text = "Las pruebas quedan en «Actividad», pero no alteran las estadísticas de llamadas reales.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun SimulationResultCard(
    result: SimResult,
    onOpenActivity: () -> Unit
) {
    val colors = AppTheme.colors
    val blocked = result.blocked
    val accent = if (blocked) colors.danger else colors.success
    val container = if (blocked) colors.dangerContainer else colors.successContainer
    val content = if (blocked) colors.onDangerContainer else colors.onSuccessContainer

    AppCard(
        containerColor = container,
        contentColor = content,
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.25f))
    ) {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(
                icon = if (blocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                container = accent.copy(alpha = 0.16f),
                content = accent,
                size = 46.dp
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (blocked) "Llamada bloqueada" else "Llamada permitida",
                    style = MaterialTheme.typography.titleLarge,
                    color = content
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = when {
                        blocked && result.ruleLabel != null ->
                            "Coincide con ${result.ruleLabel}."

                        !blocked && result.ruleLabel != null ->
                            "${result.ruleLabel} permite pasar la llamada."

                        else ->
                            "Ninguna regla coincide: la llamada pasa por defecto."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = content.copy(alpha = 0.82f)
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = content.copy(alpha = 0.16f))
        Spacer(Modifier.height(12.dp))

        ResultDetail(
            label = "Regla aplicada",
            value = result.ruleLabel ?: "Ninguna",
            content = content
        )
        if (result.priority != null) {
            Spacer(Modifier.height(7.dp))
            ResultDetail(
                label = "Prioridad",
                value = result.priority.toString(),
                content = content
            )
        }
        Spacer(Modifier.height(7.dp))
        ResultDetail(
            label = "Hora simulada",
            value = fmtHm(result.minuteOfDay),
            content = content
        )

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = content.copy(alpha = 0.75f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Guardada en Actividad",
                style = MaterialTheme.typography.labelSmall,
                color = content.copy(alpha = 0.75f)
            )
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = onOpenActivity,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Ver en Actividad",
                    style = MaterialTheme.typography.labelLarge,
                    color = content,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ResultDetail(
    label: String,
    value: String,
    content: androidx.compose.ui.graphics.Color
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = content.copy(alpha = 0.72f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ExplanationStep(
    number: Int,
    text: String
) {
    val colors = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .padding(top = 1.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(colors.surfaceSunken),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}
