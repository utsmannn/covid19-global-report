package com.utsman.covid19.api.model

data class Responses(
        val message: String,
        val total: Total,
        val data: Any?,
        val sources: List<Sources>,
        val author: String)

data class Total(
        val confirmed: Int,
        val death: Int,
        val recovered: Int
)