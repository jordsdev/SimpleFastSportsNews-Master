package com.example.simplefastnews.model


//Class Top Sport Headlines parameters from Api
data class TopSportHeadlines(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)