package com.example.simplefastnews.sportapi

import com.example.simplefastnews.model.TopSportHeadlines
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

//Interface for the News API
interface SportHeadlinesApi {

    //Get top headlines
    @GET("top-headlines")

    //Queries for the headlines
    fun getTopHeadlines(
        @Query("category") category: String,
        @Query ("country") country: String,
        @Query("apiKey") apiKey: String
    ): Observable<TopSportHeadlines>

    //Get top headlines for keyword search
    @GET("top-headlines")

    //Queries for the keyword search
    fun getUserSearchInput(
        @Query("apiKey") apiKey: String,
        @Query("q") q: String
    ): Observable<TopSportHeadlines>
}