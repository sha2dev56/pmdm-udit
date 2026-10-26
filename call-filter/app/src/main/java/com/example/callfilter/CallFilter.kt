package com.example.callfilter

object CallFilter {

    fun normalize(raw: String?): String {
        var n = raw.orEmpty().filter { it.isDigit() || it == '+' }
        n = when {
            n.startsWith("+34") -> n.removePrefix("+34")
            n.startsWith("0034") -> n.removePrefix("0034")
            else -> n
        }
        return n
    }

    fun shouldBlock(raw: String?, prefixes: List<String>): Boolean {
        val n = normalize(raw)
        return prefixes.any { it.isNotEmpty() && n.startsWith(it) }
    }
    fun mask(raw: String?): String {
        val n = normalize(raw)
        return when {
            n.isEmpty() -> "oculto"
            n.length <= 3 -> "***"
            else -> n.dropLast(3) + "***"
        }
    }
}