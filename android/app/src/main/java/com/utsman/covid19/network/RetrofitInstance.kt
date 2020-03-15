package com.utsman.covid19.network

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitInstance {

    @GET("/api/last_date")
    fun getLastDate(): Observable<ResponsesLastDate>

    @GET("/api")
    fun getData(
        @Query("day") day: Int?,
        @Query("month") month: Int?,
        @Query("year") year: Int
    ): Observable<ResponsesData>

    @GET("/api/country")
    fun getDataCountry(
        @Query("day") day: Int?,
        @Query("month") month: Int?,
        @Query("year") year: Int?,
        @Query("q") query: String? = null
    ): Observable<ResponsesCountry>

    companion object {

        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                HttpLoggingInterceptor.Level.BODY
            })
            .build()

        fun create(): RetrofitInstance {
            val builder = Retrofit.Builder()
                .baseUrl("https://covid-19-report.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build()

            return builder.create(RetrofitInstance::class.java)
        }
    }
}