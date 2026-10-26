package com.example.callfilter

import java.util.Calendar

enum class RuleType(val defaultPriority: Int) {
    PREFIX(10),
    NUMBER(50),
    WHITELIST(100)
}

data class Rule(
    val id: Long = 0,
    val type: RuleType,
    val value: String,
    val enabled: Boolean = true,
    val priority: Int = type.defaultPriority,
    val startMin: Int = -1, // -1 = sin franja horaria
    val endMin: Int = -1
) {
    fun isActiveAt(min: Int): Boolean = when {
        startMin < 0 || endMin < 0 || startMin == endMin -> true
        startMin < endMin -> min >= startMin && min < endMin
        else -> min >= startMin || min < endMin // cruza la medianoche
    }
}

data class Decision(val blocked: Boolean, val rule: Rule?)

/** "22:30" -> 1350. Vacío -> -1 (sin franja). Inválido -> null. */
fun parseHm(s: String): Int? {
    val t = s.trim()
    if (t.isEmpty()) return -1
    val parts = t.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    return if (h in 0..23 && m in 0..59) h * 60 + m else null
}

fun fmtHm(min: Int): String = "%02d:%02d".format(min / 60, min % 60)

fun currentMinutes(): Int {
    val c = Calendar.getInstance()
    return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
}