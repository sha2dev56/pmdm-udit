package com.example.callfilter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvManagerTest {

    @Test fun exportarYVolverALeer() {
        val rules = listOf(
            Rule(1, RuleType.PREFIX, "400", true, 10),
            Rule(2, RuleType.WHITELIST, "612345678", false, 100)
        )
        val parsed = CsvManager.parse(CsvManager.export(rules))
        assertEquals(2, parsed.rows.size)
        assertEquals(RuleType.WHITELIST, parsed.rows[1].type)
        assertFalse(parsed.rows[1].enabled)
        assertEquals(0, parsed.invalid)
    }

    @Test fun unaColumnaSonNumeros() {
        val parsed = CsvManager.parse("612345678\n+34 611 111 111")
        assertEquals(2, parsed.rows.size)
        assertTrue(parsed.rows.all { it.type == RuleType.NUMBER })
        assertEquals("611111111", parsed.rows[1].value)
    }

    @Test fun lineasInvalidasSeCuentan() {
        val parsed = CsvManager.parse(
            "type,value,enabled,priority\nFOO,123,1,5\nPREFIX,,1,5\nPREFIX,900,1,10"
        )
        assertEquals(1, parsed.rows.size)
        assertEquals(2, parsed.invalid)
    }
}