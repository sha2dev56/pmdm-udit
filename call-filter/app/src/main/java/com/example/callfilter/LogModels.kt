package com.example.callfilter

data class LogEntry(
    val id: Long,
    val ts: Long,
    val number: String,
    val blocked: Boolean,
    val rule: String?,
    val source: String
)

data class Stats(
    val total: Int,
    val blocked: Int,
    val byRule: List<Pair<String, Int>>
)