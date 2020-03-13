package com.utsman.covid19.api.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Data(
        val id: Int?,
        val country: String?,
        val province_or_state: String?,
        val confirmed: Int?,
        val death: Int?,
        val recovered: Int?,
        val lastUpdate: Long?,
        val coordinate: List<Double?>
)

data class Sources(
        val institution: String,
        val url: String
)