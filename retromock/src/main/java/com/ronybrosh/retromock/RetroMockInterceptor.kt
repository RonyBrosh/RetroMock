package com.ronybrosh.retromock

import android.util.Log
import okhttp3.*

class RetroMockInterceptor(private val isEnableLogging: Boolean = false) : Interceptor {
    private val mockRequestMap: MutableMap<String, MockRequest> = mutableMapOf()
    private val divider = ":"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        log("intercept: method = ${request.method()}, url = ${request.url()}")

        val response: Response = chain.proceed(request)
        val mockRequestKey: String = request.method().plus(divider).plus(request.url().toString())
        val mockRequest: MockRequest = mockRequestMap[mockRequestKey] ?: return response

        if (mockRequest.duration != null)
            Thread.sleep(mockRequest.duration)

        return response.newBuilder().apply {
            addHeader("mock-response", "RetroMock ${BuildConfig.VERSION_NAME} by Ron Brosh")
            code(mockRequest.code)
            val mediaType: MediaType = response.body()?.contentType() ?: return@apply
            val body: String = mockRequest.body ?: return@apply
            body(ResponseBody.create(mediaType, body))
        }.build()
    }

    fun addMockRequest(mockRequest: MockRequest) {
        val mockRequestKey: String = mockRequest.method.name.plus(divider).plus(mockRequest.url)
        log("addRequest: <$mockRequestKey, $mockRequest")

        mockRequestMap[mockRequestKey] = mockRequest
    }

    private fun log(message: String) {
        if (isEnableLogging)
            Log.d("RetroMockInterceptor", message)
    }
}