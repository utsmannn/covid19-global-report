package com.utsman.covid19.network

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

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
        @Query("q") query: String? = ""
    ): Observable<ResponsesCountry>

    @GET("api/articles")
    fun getArticles(
        @Query("q") query: String? = ""
    ): Observable<ResponsesArticles>

    @GET("/api/image_thumbnail")
    fun getUrlThumbnail(
        @Query("url") url: String?
    ): Observable<ResponsesImage>

    @GET("/api/stat")
    fun getStatTimeLine(
        @Query("q") country: String?
    ): Observable<ResponsesTimeLine>

    @GET("/api/sit_rep")
    fun getSitRep(): Observable<ResponseSituationReport>

    companion object {

        private fun provideLoggingInterceptor(): HttpLoggingInterceptor {

            return HttpLoggingInterceptor().apply { HttpLoggingInterceptor.Level.BODY }
        }

        private fun headerLoggingInterceptor(): HttpLoggingInterceptor {
            return HttpLoggingInterceptor().apply { HttpLoggingInterceptor.Level.HEADERS }
        }

        private fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(provideLoggingInterceptor())
                .addInterceptor(headerLoggingInterceptor())
                .callTimeout(1, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build()
        }

        fun create(): RetrofitInstance {
            val builder = Retrofit.Builder()
                .baseUrl("https://covid-19-report.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(provideOkHttpClient())
                .build()

            return builder.create(RetrofitInstance::class.java)
        }
    }
}