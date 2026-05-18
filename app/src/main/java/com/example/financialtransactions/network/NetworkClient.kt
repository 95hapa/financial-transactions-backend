package com.example.financialtransactions.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    private const val BASE_URL = "https://financial-transactions-backend.onrender.com/"

    val plaidApi: PlaidApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaidApiService::class.java)
    }
}
