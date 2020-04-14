package com.example.kino.MovieClasses

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey
    @SerializedName("id") var id: Int = 0,
    @SerializedName("vote_count") var voteCount: Int = 0,
    @SerializedName("title") var title: String = "",
    @SerializedName("vote_average") var voteAverage: Double = 0.0,
    @SerializedName("poster_path") var posterPath: String = "",
    @SerializedName("release_date") var releaseDate: String = "",
    @SerializedName("popularity") var popularity: String = "",
    @SerializedName("overview") var overview: String = "",
    @SerializedName("genre_ids")
    @Ignore
    var genreIds: ArrayList<Int>? = null,

    var runtime: Int? = null,
    var tagline: String? = null,

    @Ignore
    val genres: List<Genre>? = null,

    var genreNames: String = "",
    @Ignore
    var position: Int = 0,
    var isClicked: Boolean = false
)




