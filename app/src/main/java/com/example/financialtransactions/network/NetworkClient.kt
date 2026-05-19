package com.example.financialtransactions.network

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date

object NetworkClient {
    private const val BASE_URL = "https://financial-transactions-backend.onrender.com/"

    val plaidApi: PlaidApiService by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, JsonDeserializer { json, _, _ ->
                Date(json.asLong)
            })
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PlaidApiService::class.java)
    }
}
