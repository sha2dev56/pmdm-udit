package com.example.callfilter

data class PrefixFilter(
    val id: Long = 0,
    val prefix: String,
    val enabled: Boolean = true
)