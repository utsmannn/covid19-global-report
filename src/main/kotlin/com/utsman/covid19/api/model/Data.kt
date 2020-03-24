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

data class DataCountry(
        val country: String?,
        val total: Total,
        val data: List<Data>?
)

data class Sources(
        val institution: String,
        val url: String
)

data class Total(
        val confirmed: Int,
        val death: Int,
        val recovered: Int
)

data class TimeLine(
        val country: String,
        val timeLine: List<DataTimeLine>
)

data class DataTimeLine(
        val date: String,
        val total: Total?
)

data class LastDate(
        val day: Int?,
        val month: Int?,
        val year: Int?
)

data class Articles(
        val title: String,
        val url: String,
        val publish_date: Long,
        val publisher: String
)