package com.utsman.covid19.api.raw_model


import com.fasterxml.jackson.annotation.JsonProperty

data class RawCountries(
        @JsonProperty("name")
        var name: String?,
        @JsonProperty("alpha2Code")
        var code: String?
)
