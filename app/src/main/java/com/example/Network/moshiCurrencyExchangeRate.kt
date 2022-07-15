package com.example.Network

import com.squareup.moshi.Json

data class MoshiCurrency(@Json(name = "rates") val rates: Rates)

data class Rates(@Json(name = "TWD") val twd: TWD)

data class  TWD(@Json(name = "rate_for_amount") val rateForAmount: String)
