package com.cs4530.drawingapp.data.remote

import com.cs4530.drawingapp.data.remote.dto.AnnotateRequest
import com.cs4530.drawingapp.data.remote.dto.AnnotateResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface VisionApiService {
    @POST("v1/images:annotate")
    suspend fun annotateImage(
        @Query("key") apiKey: String,
        @Body request: AnnotateRequest
    ): AnnotateResponse

    companion object {
        const val BASE_URL = "https://vision.googleapis.com/"

        /** [baseUrl] can be overridden so tests can point the client at a local mock server. */
        fun create(baseUrl: String = BASE_URL): VisionApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VisionApiService::class.java)
        }
    }
}
