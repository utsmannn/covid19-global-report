package com.utsman.covid19.api.model

data class Responses(
        val message: String,
        val total: Total,
        val data: List<Data>?,
        val sources: List<Sources>,
        val author: String)

data class ResponsesCountry(
        val message: String,
        val total: Total,
        val countries: List<DataCountry>?,
        val sources: List<Sources>,
        val author: String)

data class ResponsesTimeLine(
        val message: String,
        val timeLine: TimeLine?,
        val sources: List<Sources>,
        val author: String
)

data class ResponsesLastDate(
        val message: String,
        val last_date_string: String?,
        val last_date: LastDate?
)

data class ResponsesArticles(
        val message: String,
        val topic: String,
        val articles: List<Articles>,
        val author: String
)

data class ResponsesImage(
        val image_url: String?
)

data class ResponseSituationReport(
        val download_url: String?
)