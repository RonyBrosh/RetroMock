package com.ronybrosh.retromock

data class MockRequest(
    val method: MockMethod,
    val url: String,
    val body: String? = null,
    val code: Int,
    val duration: Long? = null
)