package com.example.kino.MovieClasses
import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("id") val id: Int,//+
    @SerializedName("vote_count") val vote_count: Int,//+
    @SerializedName("title") val title: String,//+
    @SerializedName("vote_average") val vote_average: Float,//+
    @SerializedName("poster_path") val poster_path: String,//+
    @SerializedName("genre_ids") val genres: List<Int>, //+
    @SerializedName("original_title") val original_title: String

    //@SerializedName("popularity") val popularity: Float,//+
   // @SerializedName("release_date") val release_date: String,
    //@SerializedName("budget") val budget: Int,
   // @SerializedName("overview") val overview: String
    // @SerializedName("tagline") val tagline: String //quote e.g
    )
