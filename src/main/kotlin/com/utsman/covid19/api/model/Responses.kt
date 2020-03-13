package com.utsman.covid19.api.model

data class Responses(
        val message: String,
        val total: Int,
        val data: Any?,
        val sources: List<Sources>,
        val author: String)