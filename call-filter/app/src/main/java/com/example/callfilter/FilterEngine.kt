package com.example.callfilter

object FilterEngine {

    fun decide(raw: String?, rules: List<Rule>, nowMin: Int = currentMinutes()): Decision {
        val n = CallFilter.normalize(raw)
        if (n.isEmpty()) return Decision(false, null) // número oculto: se deja pasar

        val match = rules
            .filter { it.enabled && it.value.isNotEmpty() && it.isActiveAt(nowMin) && matches(it, n) }
            .sortedWith(
                compareByDescending<Rule> { it.priority }
                    .thenBy { if (it.type == RuleType.WHITELIST) 0 else 1 }
            )
            .firstOrNull()

        return when {
            match == null -> Decision(false, null)
            match.type == RuleType.WHITELIST -> Decision(false, match)
            else -> Decision(true, match)
        }
    }

    private fun matches(rule: Rule, n: String): Boolean = when (rule.type) {
        RuleType.PREFIX -> n.startsWith(rule.value)
        RuleType.NUMBER, RuleType.WHITELIST -> n == rule.value
    }
}