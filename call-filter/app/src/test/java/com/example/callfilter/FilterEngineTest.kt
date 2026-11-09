package com.example.callfilter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del motor de decisión: reglas, prioridades, lista de permitidos y franjas horarias.
 * Siempre se pasa `nowMin` para que los tests no dependan de la hora real.
 */
class FilterEngineTest {

    private val noon = 12 * 60

    private val block400 = Rule(type = RuleType.PREFIX, value = "400")

    private fun decide(number: String?, vararg rules: Rule, now: Int = noon) =
        FilterEngine.decide(number, rules.toList(), now)

    // ---------- Regla del prefijo 400 ----------

    @Test
    fun blocks_400_in_every_spanish_format() {
        val formats = listOf(
            "400123456",
            "+34400123456",
            "0034400123456",
            "+34 400 123 456",
            "0034-400-123-456",
            "(400) 123 456"
        )
        formats.forEach { number ->
            val d = decide(number, block400)
            assertTrue("debería bloquear $number", d.blocked)
            assertEquals("400", d.rule?.value)
        }
    }

    @Test
    fun allows_numbers_that_do_not_start_with_400() {
        listOf("612345678", "+34612345678", "914001234", "+44 400 123 456", "0044400123456")
            .forEach { number ->
                val d = decide(number, block400)
                assertFalse("debería permitir $number", d.blocked)
                assertNull(d.rule)
            }
    }

    @Test
    fun hidden_number_is_allowed() {
        assertFalse(decide(null, block400).blocked)
        assertFalse(decide("", block400).blocked)
    }

    @Test
    fun no_rules_means_allow() {
        val d = FilterEngine.decide("400123456", emptyList(), noon)
        assertFalse(d.blocked)
        assertNull(d.rule)
    }

    // ---------- Reglas activas y válidas ----------

    @Test
    fun disabled_rule_is_ignored() {
        assertFalse(decide("400123456", block400.copy(enabled = false)).blocked)
    }

    @Test
    fun empty_rule_value_is_ignored() {
        assertFalse(decide("400123456", Rule(type = RuleType.PREFIX, value = "")).blocked)
    }

    @Test
    fun exact_number_rule_matches_only_that_number() {
        val rule = Rule(type = RuleType.NUMBER, value = "612345678")
        assertTrue(decide("+34 612 345 678", rule).blocked)
        assertFalse(decide("612345679", rule).blocked)
    }

    // ---------- Lista de permitidos y prioridad ----------

    @Test
    fun whitelist_beats_prefix_by_default_priority() {
        val allow = Rule(type = RuleType.WHITELIST, value = "400123456")
        val d = decide("+34400123456", block400, allow)
        assertFalse(d.blocked)
        assertEquals(RuleType.WHITELIST, d.rule?.type)
    }

    @Test
    fun whitelist_does_not_affect_other_400_numbers() {
        val allow = Rule(type = RuleType.WHITELIST, value = "400123456")
        assertTrue(decide("400999999", block400, allow).blocked)
    }

    @Test
    fun higher_priority_block_beats_lower_priority_whitelist() {
        val allow = Rule(type = RuleType.WHITELIST, value = "400123456", priority = 5)
        assertTrue(decide("400123456", block400, allow).blocked)
    }

    @Test
    fun whitelist_wins_a_priority_tie() {
        val allow = Rule(type = RuleType.WHITELIST, value = "400123456", priority = 10)
        assertFalse(decide("400123456", block400, allow).blocked)
    }

    @Test
    fun exact_number_block_beats_prefix_rule_in_the_log() {
        val exact = Rule(type = RuleType.NUMBER, value = "400123456")
        val d = decide("400123456", block400, exact)
        assertTrue(d.blocked)
        assertEquals(RuleType.NUMBER, d.rule?.type)
    }

    // ---------- Franjas horarias ----------

    @Test
    fun overnight_window_blocks_inside_and_allows_outside() {
        val night = block400.copy(startMin = 22 * 60, endMin = 7 * 60) // 22:00 a 07:00
        assertTrue(decide("400123456", night, now = 23 * 60).blocked)
        assertTrue(decide("400123456", night, now = 3 * 60).blocked)
        assertFalse(decide("400123456", night, now = 12 * 60).blocked)
    }

    @Test
    fun same_day_window_blocks_only_inside() {
        val office = block400.copy(startMin = 9 * 60, endMin = 18 * 60)
        assertTrue(decide("400123456", office, now = 10 * 60).blocked)
        assertFalse(decide("400123456", office, now = 20 * 60).blocked)
        assertFalse(decide("400123456", office, now = 18 * 60).blocked) // el final es exclusivo
    }

    @Test
    fun rule_without_window_is_always_active() {
        assertTrue(block400.isActiveAt(0))
        assertTrue(block400.isActiveAt(23 * 60 + 59))
        assertTrue(block400.copy(startMin = 600, endMin = 600).isActiveAt(100))
    }

    // ---------- Utilidades de hora ----------

    @Test
    fun parseHm_accepts_valid_times() {
        assertEquals(22 * 60 + 30, parseHm("22:30"))
        assertEquals(0, parseHm("00:00"))
        assertEquals(-1, parseHm(""))
        assertEquals(-1, parseHm("   "))
    }

    @Test
    fun parseHm_rejects_invalid_times() {
        assertNull(parseHm("25:00"))
        assertNull(parseHm("12:60"))
        assertNull(parseHm("abc"))
        assertNull(parseHm("12"))
        assertNull(parseHm("12:30:00"))
    }
}