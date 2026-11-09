package com.example.callfilter

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------- Textos de dominio ----------

fun friendlyRule(desc: String): String {
    val parts = desc.split(":", limit = 2)
    if (parts.size < 2) return desc
    return when (parts[0]) {
        "PREFIX" -> "Prefijo ${parts[1]}"
        "NUMBER" -> "Número ${parts[1]}"
        "WHITELIST" -> "Permitido ${parts[1]}"
        else -> desc
    }
}

fun typeName(t: RuleType): String = when (t) {
    RuleType.PREFIX -> "Prefijos"
    RuleType.NUMBER -> "Números"
    RuleType.WHITELIST -> "Permitidos"
}

/** Nombre en singular, más natural dentro de una tarjeta o un diálogo. */
fun typeLabel(t: RuleType): String = when (t) {
    RuleType.PREFIX -> "Prefijo"
    RuleType.NUMBER -> "Número exacto"
    RuleType.WHITELIST -> "Número permitido"
}

/** Texto del botón de añadir según la lista activa. */
fun typeAddLabel(t: RuleType): String = when (t) {
    RuleType.PREFIX -> "Añadir prefijo"
    RuleType.NUMBER -> "Añadir número"
    RuleType.WHITELIST -> "Añadir permitido"
}

/** Traduce la prioridad numérica a algo entendible. */
fun priorityLabel(priority: Int): String = when {
    priority >= 80 -> "Prioridad alta"
    priority >= 50 -> "Prioridad media"
    else -> "Prioridad baja"
}

fun typeIcon(t: RuleType): ImageVector = when (t) {
    RuleType.PREFIX -> Icons.Default.Call
    RuleType.NUMBER -> Icons.Default.Person
    RuleType.WHITELIST -> Icons.Default.CheckCircle
}

fun typeHelp(t: RuleType): String = when (t) {
    RuleType.PREFIX -> "Bloquea las llamadas que empiezan por estos dígitos."
    RuleType.NUMBER -> "Bloquea números concretos, uno por entrada."
    RuleType.WHITELIST -> "Estos números siempre pasan, salvo que un bloqueo tenga más prioridad."
}

// ---------- Identidad visual ----------

/** Escudo dibujado a mano: la marca gráfica de CallFilter. */
@Composable
fun ShieldMark(
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.colors.primary,
    checkTint: Color = AppTheme.colors.background,
    filled: Boolean = true
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val shield = Path().apply {
            moveTo(w * 0.50f, h * 0.04f)
            lineTo(w * 0.90f, h * 0.20f)
            lineTo(w * 0.90f, h * 0.50f)
            cubicTo(w * 0.90f, h * 0.73f, w * 0.73f, h * 0.89f, w * 0.50f, h * 0.97f)
            cubicTo(w * 0.27f, h * 0.89f, w * 0.10f, h * 0.73f, w * 0.10f, h * 0.50f)
            lineTo(w * 0.10f, h * 0.20f)
            close()
        }
        if (filled) {
            drawPath(shield, tint)
        } else {
            drawPath(
                shield,
                tint,
                style = Stroke(width = w * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        val check = Path().apply {
            moveTo(w * 0.31f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.63f)
            lineTo(w * 0.70f, h * 0.36f)
        }
        drawPath(
            check,
            if (filled) checkTint else tint,
            style = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    val colors = AppTheme.colors
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.26f))
            .background(colors.primary),
        contentAlignment = Alignment.Center
    ) {
        ShieldMark(
            modifier = Modifier.size(size * 0.58f),
            tint = colors.onPrimary,
            checkTint = colors.primary
        )
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.26f))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    container: Color,
    content: Color,
    showDot: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showDot) {
            Box(
                Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(content)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.primary)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Tarjeta base: superficie tonal, borde fino y sin sombras pesadas. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = AppTheme.colors.surface,
    contentColor: Color = AppTheme.colors.textPrimary,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    shape: Shape = MaterialTheme.shapes.medium,
    border: BorderStroke? = BorderStroke(1.dp, AppTheme.colors.border),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = shape,
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

enum class NoticeTone { INFO, SUCCESS, WARNING, DANGER }

/** Aviso compacto y accionable para permisos, estados y consejos. */
@Composable
fun NoticeCard(
    icon: ImageVector,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.INFO,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    val container: Color
    val content: Color
    when (tone) {
        NoticeTone.INFO -> {
            container = colors.primary.copy(alpha = if (colors.isDark) 0.12f else 0.07f)
            content = colors.primary
        }
        NoticeTone.SUCCESS -> {
            container = colors.successContainer
            content = colors.onSuccessContainer
        }
        NoticeTone.WARNING -> {
            container = colors.warningContainer
            content = colors.onWarningContainer
        }
        NoticeTone.DANGER -> {
            container = colors.dangerContainer
            content = colors.onDangerContainer
        }
    }

    AppCard(
        modifier = modifier,
        containerColor = container,
        contentColor = content,
        border = BorderStroke(1.dp, content.copy(alpha = 0.18f)),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(
                icon = icon,
                container = content.copy(alpha = 0.12f),
                content = content,
                size = 38.dp
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = content
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = content.copy(alpha = 0.82f)
                )
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(6.dp))
                    TextButton(
                        onClick = onAction,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = content)
                    ) {
                        Text(actionLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceSunken)
                .border(1.dp, colors.border, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = colors.textSecondary
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onAction,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun Metric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = AppTheme.colors.textPrimary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/** Paso numerado de una guía de inicio. */
@Composable
fun StepRow(
    number: Int,
    title: String,
    text: String,
    done: Boolean,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (done) colors.successContainer else colors.surfaceSunken),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.success
                )
            } else {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        if (!done && actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ---------- Ajustes y navegación ----------

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    danger: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = AppTheme.colors
    val accent = if (danger) colors.danger else colors.textSecondary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .heightIn(min = 64.dp)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = icon,
            container = if (danger) colors.dangerContainer else colors.surfaceSunken,
            content = accent,
            size = 38.dp
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = if (danger) colors.danger else colors.textPrimary
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    AppCard(contentPadding = PaddingValues(0.dp)) {
        content()
    }
}

@Composable
fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp, end = 18.dp),
        color = AppTheme.colors.border
    )
}

// ---------- Controles ----------

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Borrar búsqueda",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.border,
            cursorColor = colors.primary,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
        )
    )
}

data class SegmentOption(
    val label: String,
    val icon: ImageVector? = null
)

@Composable
fun SegmentedPills(
    options: List<SegmentOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val colors = AppTheme.colors
    val motionEnabled = rememberMotionEnabled()
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceSunken)
            .border(1.dp, colors.border, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val background by animateColorAsState(
                targetValue = if (selected) colors.surfaceElevated else Color.Transparent,
                animationSpec = if (motionEnabled) tween(180) else snap(),
                label = "segmentBackground"
            )
            val foreground by animateColorAsState(
                targetValue = if (selected) colors.textPrimary else colors.textSecondary,
                animationSpec = if (motionEnabled) tween(180) else snap(),
                label = "segmentForeground"
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = if (compact) 36.dp else 42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(background)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (option.icon != null) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = foreground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = option.label,
                    style = if (compact) {
                        MaterialTheme.typography.labelMedium
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                    color = foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true,
    icon: ImageVector = Icons.Default.Warning
) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            IconBadge(
                icon = icon,
                container = if (destructive) colors.dangerContainer else colors.primary.copy(alpha = 0.14f),
                content = if (destructive) colors.danger else colors.primary,
                size = 48.dp
            )
        },
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (destructive) colors.danger else colors.primary,
                    contentColor = if (destructive) colors.onDanger else colors.onPrimary
                )
            ) {
                Text(confirmLabel, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", style = MaterialTheme.typography.labelLarge)
            }
        },
        containerColor = colors.surfaceElevated,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        shape = MaterialTheme.shapes.large
    )
}

/** Separador con etiqueta central, usado en listas agrupadas. */
@Composable
fun LabeledDivider(label: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f), color = AppTheme.colors.border)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        HorizontalDivider(Modifier.weight(1f), color = AppTheme.colors.border)
    }
}