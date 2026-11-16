package com.cs4530.drawingapp.data.remote

import com.cs4530.drawingapp.data.remote.dto.AnnotateRequest
import com.cs4530.drawingapp.data.remote.dto.AnnotateResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface VisionApiService {
    @POST("v1/images:annotate")
    suspend fun annotateImage(
        @Query("key") apiKey: String,
        @Body request: AnnotateRequest
    ): AnnotateResponse

    companion object {
        private const val BASE_URL = "https://vision.googleapis.com/"

        fun create(): VisionApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VisionApiService::class.java)
        }
    }
}
