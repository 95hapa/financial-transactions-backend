package com.example.financialtransactions.network

import com.example.financialtransactions.model.Account
import com.example.financialtransactions.model.Transaction
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PlaidApiService {
    @GET("api/link_token")
    suspend fun getLinkToken(): LinkTokenResponse

    @POST("api/exchange_public_token")
    suspend fun exchangePublicToken(@Body request: ExchangeRequest): ExchangeResponse

    @POST("api/transactions")
    suspend fun getTransactions(@Body request: TransactionsRequest): List<Transaction>

    @POST("api/accounts")
    suspend fun getAccounts(@Body request: AccountsRequest): List<Account>

    @POST("api/unlink_account")
    suspend fun unlinkAccount(@Body request: UnlinkRequest): UnlinkResponse
}

data class LinkTokenResponse(val link_token: String)
data class ExchangeRequest(val public_token: String)
data class ExchangeResponse(val success: Boolean, val access_token: String, val item_id: String)
data class TransactionsRequest(val access_tokens: List<String>)
data class AccountsRequest(val access_tokens: List<String>)
data class UnlinkRequest(val access_token: String)
data class UnlinkResponse(val success: Boolean)
