package com.example.callfilter

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterEngineTest {

    private fun rule(type: RuleType, value: String, priority: Int = type.defaultPriority, enabled: Boolean = true) =
        Rule(1, type, value, enabled, priority)

    @Test fun prefijoBloquea() {
        val d = FilterEngine.decide("+34400123456", listOf(rule(RuleType.PREFIX, "400")))
        assertTrue(d.blocked)
    }

    @Test fun listaBlancaGanaAlPrefijo() {
        val rules = listOf(rule(RuleType.PREFIX, "400"), rule(RuleType.WHITELIST, "400123456"))
        assertFalse(FilterEngine.decide("400123456", rules).blocked)
    }

    @Test fun prioridadAltaDelBloqueoGanaALaListaBlanca() {
        val rules = listOf(
            rule(RuleType.WHITELIST, "400123456", priority = 5),
            rule(RuleType.NUMBER, "400123456", priority = 50)
        )
        assertTrue(FilterEngine.decide("400123456", rules).blocked)
    }

    @Test fun empateGanaLaListaBlanca() {
        val rules = listOf(
            rule(RuleType.NUMBER, "400123456", priority = 10),
            rule(RuleType.WHITELIST, "400123456", priority = 10)
        )
        assertFalse(FilterEngine.decide("400123456", rules).blocked)
    }

    @Test fun reglaDesactivadaNoAplica() {
        assertFalse(FilterEngine.decide("400123456", listOf(rule(RuleType.PREFIX, "400", enabled = false))).blocked)
    }

    @Test fun numeroOcultoSePermite() {
        assertFalse(FilterEngine.decide(null, listOf(rule(RuleType.PREFIX, "400"))).blocked)
    }
    @Test fun franjaNocturnaCruzaMedianoche() {
        val r = Rule(1, RuleType.PREFIX, "400", true, 10, startMin = 22 * 60, endMin = 8 * 60)
        assertTrue(FilterEngine.decide("400123456", listOf(r), nowMin = 23 * 60).blocked)
        assertTrue(FilterEngine.decide("400123456", listOf(r), nowMin = 3 * 60).blocked)
        assertFalse(FilterEngine.decide("400123456", listOf(r), nowMin = 12 * 60).blocked)
    }

    @Test fun franjaDiurnaExcluyeElFinal() {
        val r = Rule(1, RuleType.NUMBER, "612345678", true, 50, startMin = 9 * 60, endMin = 18 * 60)
        assertTrue(FilterEngine.decide("612345678", listOf(r), nowMin = 10 * 60).blocked)
        assertFalse(FilterEngine.decide("612345678", listOf(r), nowMin = 18 * 60).blocked)
        assertFalse(FilterEngine.decide("612345678", listOf(r), nowMin = 20 * 60).blocked)
    }
}