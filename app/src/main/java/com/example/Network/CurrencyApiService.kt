package com.example.Network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val currencyURL: String = "https://api.getgeoapi.com"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(currencyURL)
    .build()

interface CurrencyApiService{
    @GET("v2/currency/convert")
    suspend fun getCurrencyJSON(@Query("api_key") api_key: String,
                                @Query("from") from: String,
                                @Query("to") to: String,
                                @Query("amount") amount: String,
                                @Query("format") format: String): MoshiCurrency
}

object currencyAPI{
    val retrofitService: CurrencyApiService by lazy{
        retrofit.create(CurrencyApiService::class.java)
    }
}