package com.utsman.covid19.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.utsman.covid19.Data
import com.utsman.covid19.Responses
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RetrofitInstance {

    @GET("/api")
    fun getData(
        @Query("day") day: Int,
        @Query("month") month: Int,
        @Query("year") year: Int,
        @Query("q") query: String? = null
    ): Observable<Responses>

    companion object {

        private val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setLenient()
            .create()

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
                .build()

            return builder.create(RetrofitInstance::class.java)
        }
    }
}