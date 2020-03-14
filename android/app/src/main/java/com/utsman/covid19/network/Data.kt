package com.utsman.covid19.network

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class ResponsesData(
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

data class DataCountry(
    val country: String?,
    val total: Total,
    val data: List<Data>?
)

data class Data(
    val id: Int?,
    val country: String,
    @SerializedName("province_or_state")
    val provinceOrState: String?,
    val confirmed: Int?,
    val death: Int?,
    val recovered: Int?,
    val lastUpdate: Long?,
    val coordinate: List<Double>
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

data class ItemMarker(
    val title: String,
    val latLng: List<LatLng>
)

data class ItemCluster(
    val title: Int,
    val latLng: LatLng,
    val data: Data
)