package com.example.callfilter

data class ParsedRow(
    val type: RuleType,
    val value: String,
    val enabled: Boolean,
    val priority: Int,
    val startMin: Int = -1,
    val endMin: Int = -1
)
data class ParseResult(val rows: List<ParsedRow>, val invalid: Int)
data class ImportResult(val added: Int, val skipped: Int)

object CsvManager {
    const val HEADER = "type,value,enabled,priority,start,end"

    fun export(rules: List<Rule>): String =
        (listOf(HEADER) + rules.map {
            val s = if (it.startMin >= 0) fmtHm(it.startMin) else ""
            val e = if (it.endMin >= 0) fmtHm(it.endMin) else ""
            "${it.type.name},${it.value},${if (it.enabled) 1 else 0},${it.priority},$s,$e"
        }).joinToString("\n")

    fun parse(text: String): ParseResult {
        val rows = mutableListOf<ParsedRow>()
        var invalid = 0
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEachIndexed { i, line ->
                if (i == 0 && line.lowercase().startsWith("type")) return@forEachIndexed // cabecera

                val p = line.split(",").map { it.trim() }
                val type: RuleType?
                val rawValue: String
                if (p.size == 1) {
                    type = RuleType.NUMBER
                    rawValue = p[0]
                } else {
                    type = runCatching { RuleType.valueOf(p[0].uppercase()) }.getOrNull()
                    rawValue = p[1]
                }
                val value = CallFilter.normalize(rawValue)
                if (type == null || value.isEmpty()) {
                    invalid++
                    return@forEachIndexed
                }
                val enabled = p.getOrNull(2) != "0"
                val priority = p.getOrNull(3)?.toIntOrNull() ?: type.defaultPriority
                val start = p.getOrNull(4)?.let { parseHm(it) } ?: -1
                val end = p.getOrNull(5)?.let { parseHm(it) } ?: -1
                val (s, e) = if ((start < 0) != (end < 0)) (-1 to -1) else (start to end)
                rows.add(ParsedRow(type, value, enabled, priority, s, e))
            }
        return ParseResult(rows, invalid)
    }

    fun import(text: String, db: FilterDbHelper): ImportResult {
        val parsed = parse(text)
        var added = 0
        var skipped = parsed.invalid
        for (r in parsed.rows) {
            val id = db.insertRule(r.type, r.value, r.priority, r.startMin, r.endMin)
            if (id == -1L) {
                skipped++ // duplicado
            } else {
                added++
                if (!r.enabled) db.setRuleEnabled(id, false)
            }
        }
        return ImportResult(added, skipped)
    }
}