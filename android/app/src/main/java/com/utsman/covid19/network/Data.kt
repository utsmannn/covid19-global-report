package com.utsman.covid19.network

import android.annotation.SuppressLint
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

data class ResponsesLastDate(
    val message: String,
    @SerializedName("last_date_string")
    val lastDateString: String?,
    @SerializedName("last_date")
    val lastDate: LastDate?
)

data class ResponsesArticles(
    val message: String,
    val topic: String,
    val articles: List<Articles>,
    val author: String
)

data class ResponsesImage(
    @SerializedName("image_url")
    val imageUrl: String?
)

data class ResponsesTimeLine(
    val message: String,
    val timeLine: TimeLine?,
    val sources: List<Sources>,
    val author: String
)

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

data class LastDate(
    val day: Int?,
    val month: Int?,
    val year: Int?
)

data class Articles(
    val title: String,
    val url: String,
    @SerializedName("publish_date")
    val publishDate: Long,
    val publisher: String,
    var imgUrl: String? = null
)

data class TimeLine(
    val country: String,
    val timeLine: List<DataTimeLine>
)

data class DataTimeLine(
    val date: String,
    val total: Total
)