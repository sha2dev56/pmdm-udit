package com.example.callfilter

import android.telecom.Call
import android.telecom.CallScreeningService

class CallFilterService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val incoming = callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        val number = callDetails.handle?.schemeSpecificPart
        val db = FilterDbHelper.get(applicationContext)

        // si algo falla leyendo la BD, la llamada se deja pasar
        val decision = if (incoming) {
            runCatching { FilterEngine.decide(number, db.getActiveRules()) }
                .getOrDefault(Decision(false, null))
        } else Decision(false, null)

        val builder = CallResponse.Builder()
        if (decision.blocked) {
            builder.setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(true)
        }
        respondToCall(callDetails, builder.build()) // primero responder...

        if (incoming) { // ...y después registrar
            runCatching {
                db.logCall(
                    CallFilter.mask(number),
                    decision.blocked,
                    decision.rule?.let { "${it.type}:${it.value}" },
                    "REAL"
                )
            }
        }
    }
}