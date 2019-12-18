package com.ronybrosh.retromock

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

class RetroMockInterceptorTest {
    private lateinit var demoApi: DemoApi

    @Before
    fun before() {
        val retrofit = Retrofit.Builder().apply {
            baseUrl("https://api.spacexdata.com/")
            client(OkHttpClient.Builder().apply {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                addInterceptor(RetroMockInterceptor(isEnableLogging = true).apply {
                    addMockRequest(
                        MockRequest(
                            MockMethod.POST,
                            "https://api.spacexdata.com/mock-endpoint",
                            "Hello RetroMock success",
                            200,
                            5000
                        )
                    )
                    addMockRequest(
                        MockRequest(
                            MockMethod.PATCH,
                            "https://api.spacexdata.com/mock-endpoint",
                            "Hello RetroMock fail",
                            404
                        )
                    )
                })
            }.build())
        }.build()

        demoApi = retrofit.create(DemoApi::class.java)
    }

    @Test
    fun callingRealApi_shouldReturnResult() {
        val response: Response<Unit> = demoApi.callRealEndpoint().execute()
        assert(response.code() == 200)
    }

    @Test
    fun callingSuccessMockApi_shouldReturnResultWithBody() {
        val response: Response<Unit> = demoApi.callSuccessMockEndpoint().execute()
        assert(response.code() == 200)
        assert(response.body() ?: "" == "Hello RetroMock success")
    }

    @Test
    fun callingFailMockApi_shouldReturn404ErrorCode() {
        val response: Response<Unit> = demoApi.callFailMockEndpoint().execute()
        assert(response.code() == 404)
        assert(response.body() ?: "" == "Hello RetroMock fail")
    }

    interface DemoApi {
        @GET("v3/rockets")
        fun callRealEndpoint(): Call<Unit>

        @POST("mock-endpoint")
        fun callSuccessMockEndpoint(): Call<Unit>

        @PATCH("mock-endpoint")
        fun callFailMockEndpoint(): Call<Unit>
    }
}