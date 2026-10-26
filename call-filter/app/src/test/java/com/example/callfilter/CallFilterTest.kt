package com.example.callfilter

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class CallFilterTest {
    private val prefixes = listOf("400")

    @Test fun bloquea() {
        assertTrue(CallFilter.shouldBlock("400123456", prefixes))
        assertTrue(CallFilter.shouldBlock("+34400123456", prefixes))
        assertTrue(CallFilter.shouldBlock("+34 400 123 456", prefixes))
        assertTrue(CallFilter.shouldBlock("0034400123456", prefixes))
    }

    @Test fun permite() {
        assertFalse(CallFilter.shouldBlock("612345678", prefixes))
        assertFalse(CallFilter.shouldBlock(null, prefixes))
        assertFalse(CallFilter.shouldBlock("400123456", emptyList()))
    }

    @Test fun variosPrefijos() {
        assertTrue(CallFilter.shouldBlock("900123456", listOf("400", "900")))
    }
}