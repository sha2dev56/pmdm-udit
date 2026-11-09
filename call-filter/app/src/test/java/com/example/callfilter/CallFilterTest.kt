package com.example.callfilter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de la normalización y de la regla del prefijo 400.
 * Son tests JVM puros: no necesitan emulador ni dispositivo.
 */
class CallFilterTest {

    private val prefixes = listOf("400")

    // ---------- normalize ----------

    @Test
    fun normalize_removes_plus34() {
        assertEquals("400123456", CallFilter.normalize("+34400123456"))
    }

    @Test
    fun normalize_removes_0034() {
        assertEquals("400123456", CallFilter.normalize("0034400123456"))
    }

    @Test
    fun normalize_strips_spaces_dashes_dots_and_parentheses() {
        assertEquals("400123456", CallFilter.normalize("+34 400 123 456"))
        assertEquals("400123456", CallFilter.normalize("0034-400-123-456"))
        assertEquals("400123456", CallFilter.normalize("(+34) 400.123.456"))
        assertEquals("400123456", CallFilter.normalize("(400) 123 456"))
    }

    @Test
    fun normalize_keeps_foreign_prefix() {
        assertEquals("+44400123456", CallFilter.normalize("+44 400 123 456"))
        assertEquals("0044400123456", CallFilter.normalize("0044 400 123 456"))
    }

    @Test
    fun normalize_returns_empty_for_null_blank_or_text() {
        assertEquals("", CallFilter.normalize(null))
        assertEquals("", CallFilter.normalize(""))
        assertEquals("", CallFilter.normalize("   "))
        assertEquals("", CallFilter.normalize("abc"))
        assertEquals("", CallFilter.normalize("+34"))
    }

    // ---------- Se deben BLOQUEAR (prefijo 400) ----------

    @Test
    fun blocks_plain_400() = assertTrue(CallFilter.shouldBlock("400123456", prefixes))

    @Test
    fun blocks_plus34_400() = assertTrue(CallFilter.shouldBlock("+34400123456", prefixes))

    @Test
    fun blocks_0034_400() = assertTrue(CallFilter.shouldBlock("0034400123456", prefixes))

    @Test
    fun blocks_with_separators() {
        assertTrue(CallFilter.shouldBlock("+34 400 123 456", prefixes))
        assertTrue(CallFilter.shouldBlock("0034-400-123-456", prefixes))
        assertTrue(CallFilter.shouldBlock("(400) 123 456", prefixes))
    }

    @Test
    fun blocks_bare_prefix() {
        assertTrue(CallFilter.shouldBlock("400", prefixes))
        assertTrue(CallFilter.shouldBlock("+34400", prefixes))
    }

    // ---------- Se deben PERMITIR ----------

    @Test
    fun allows_normal_mobile_and_landline() {
        assertFalse(CallFilter.shouldBlock("612345678", prefixes))
        assertFalse(CallFilter.shouldBlock("+34612345678", prefixes))
        assertFalse(CallFilter.shouldBlock("0034612345678", prefixes))
        assertFalse(CallFilter.shouldBlock("+34 91 123 45 67", prefixes))
    }

    @Test
    fun allows_400_not_at_start() {
        assertFalse(CallFilter.shouldBlock("914001234", prefixes))
        assertFalse(CallFilter.shouldBlock("+34 91 400 12 34", prefixes))
        assertFalse(CallFilter.shouldBlock("5400123456", prefixes))
    }

    @Test
    fun allows_foreign_numbers_containing_400() {
        assertFalse(CallFilter.shouldBlock("+44 400 123 456", prefixes))
        assertFalse(CallFilter.shouldBlock("0044400123456", prefixes))
        assertFalse(CallFilter.shouldBlock("+1 400 555 0100", prefixes))
    }

    @Test
    fun allows_partial_prefix_hidden_number_and_garbage() {
        assertFalse(CallFilter.shouldBlock("40", prefixes))
        assertFalse(CallFilter.shouldBlock("4", prefixes))
        assertFalse(CallFilter.shouldBlock("+34", prefixes))
        assertFalse(CallFilter.shouldBlock(null, prefixes))
        assertFalse(CallFilter.shouldBlock("", prefixes))
        assertFalse(CallFilter.shouldBlock("abc", prefixes))
    }

    @Test
    fun ignores_empty_prefixes_and_empty_list() {
        assertFalse(CallFilter.shouldBlock("400123456", listOf("")))
        assertFalse(CallFilter.shouldBlock("400123456", emptyList()))
    }

    @Test
    fun blocks_with_several_prefixes() {
        assertTrue(CallFilter.shouldBlock("900123456", listOf("400", "900")))
        assertTrue(CallFilter.shouldBlock("+34400123456", listOf("400", "900")))
        assertFalse(CallFilter.shouldBlock("612345678", listOf("400", "900")))
    }

    // ---------- mask (privacidad del registro) ----------

    @Test
    fun mask_hides_last_three_digits() {
        assertEquals("612345***", CallFilter.mask("+34 612 345 678"))
        assertEquals("400123***", CallFilter.mask("0034400123456"))
        assertEquals("1***", CallFilter.mask("1234"))
    }

    @Test
    fun mask_handles_hidden_and_short_numbers() {
        assertEquals("oculto", CallFilter.mask(null))
        assertEquals("oculto", CallFilter.mask(""))
        assertEquals("***", CallFilter.mask("400"))
        assertEquals("***", CallFilter.mask("12"))
    }
}