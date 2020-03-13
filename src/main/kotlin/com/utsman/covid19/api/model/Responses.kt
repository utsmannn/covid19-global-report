package com.utsman.covid19.api.model

data class Responses(
        val message: String,
        val sources: List<Sources>,
        val total: Int,
        val data: Any?,
        val author: String)