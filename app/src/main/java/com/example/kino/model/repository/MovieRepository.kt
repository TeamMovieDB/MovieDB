package com.example.kino.model.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.kino.R
import com.example.kino.model.database.MovieDao
import com.example.kino.model.database.MovieStatusDao
import com.example.kino.model.movie.Genres
import com.example.kino.model.movie.Movie
import com.example.kino.model.movie.MovieStatus
import com.example.kino.model.movie.SelectedMovie
import com.example.kino.utils.ApiResponse
import com.example.kino.utils.PostApi
import com.example.kino.utils.constants.NULLABLE_VALUE
import com.example.kino.utils.constants.RESPONSE_ERROR
import io.reactivex.Single

interface MovieRepository {
    fun getLocalMovies(): List<Movie>?
    fun getLocalMovie(id: Int): Movie?
    fun getLocalFavouriteMovies(): List<Movie>?
    fun insertLocalMovies(movies: List<Movie>)
    fun updateLocalMovieIsCLicked(isClicked: Boolean, id: Int)
    fun updateLocalMovieProperties(tagline: String, runtime: Int, id: Int)
    fun deleteLocalMovies()

    fun getLocalMovieStatuses(): List<MovieStatus>?
    fun insertLocalMovieStatus(movieState: MovieStatus)
    fun deleteLocalMovieStatuses()
    fun getLocalSessionId(context: Context): String

    suspend fun getRemoteGenres(apiKey: String): Genres?
    fun getRemoteMovie(id: Int, apiKey: String): Single<ApiResponse<Movie>>?
    fun getRemoteMovieList(apiKey: String, page: Int): Single<ApiResponse<List<Movie>>>?
    fun getRemoteFavouriteMovies(
        apiKey: String,
        sessionId: String
    ): Single<ApiResponse<List<Movie>>>?

    fun getRemoteMovieStateRX(
        movieId: Int,
        apiKey: String,
        sessionId: String
    ): Single<ApiResponse<MovieStatus>>?

    fun updateRemoteFavouritesRX(
        apiKey: String,
        sessionId: String,
        fav: SelectedMovie
    ): Single<ApiResponse<Boolean>>?

}

class MovieRepositoryImpl(
    private var movieDao: MovieDao? = null,
    private var service: PostApi? = null,
    private var movieStatusDao: MovieStatusDao? = null,
    private var sharedPreferences: SharedPreferences
) : MovieRepository {

    override fun getLocalMovies(): List<Movie>? {
        return movieDao?.getMovies()
    }

    override fun getLocalFavouriteMovies(): List<Movie>? {
        return movieDao?.getFavouriteMovies()
    }

    override fun insertLocalMovies(movies: List<Movie>) {
        movieDao?.insertAll(movies)
    }

    override fun deleteLocalMovies() {
        movieDao?.deleteAll()
    }

    override fun updateLocalMovieIsCLicked(isClicked: Boolean, id: Int) {
        movieDao?.updateMovieIsCLicked(isClicked, id)
    }

    override fun updateLocalMovieProperties(tagline: String, runtime: Int, id: Int) {
        movieDao?.updateMovieProperties(tagline, runtime, id)
    }

    override fun getLocalMovie(id: Int): Movie? {
        return movieDao?.getMovie(id)
    }

    override fun insertLocalMovieStatus(movieState: MovieStatus) {
        movieStatusDao?.insertMovieStatus(movieState)
    }

    override fun getLocalMovieStatuses(): List<MovieStatus>? {
        return movieStatusDao?.getMovieStatuses()
    }

    override fun deleteLocalMovieStatuses() {
        movieStatusDao?.deleteAll()
    }

    override suspend fun getRemoteGenres(apiKey: String): Genres? {
        return service?.getGenres(apiKey)?.body()
    }

    override fun getRemoteMovie(id: Int, apiKey: String): Single<ApiResponse<Movie>>? {
        return service?.getMovieById(id, apiKey)
            ?.map { response ->
                if (response.isSuccessful) {
                    val movie = response.body() ?: Movie()
                    ApiResponse.Success(movie)
                } else {
                    ApiResponse.Error<Movie>(RESPONSE_ERROR)
                }
            }
    }

    override fun getRemoteMovieStateRX(
        movieId: Int,
        apiKey: String,
        sessionId: String
    ): Single<ApiResponse<MovieStatus>>? {
        return service?.getMovieStatesRX(movieId, apiKey, sessionId)
            ?.map { response ->
                if (response.isSuccessful) {
                    val status = response.body()!!
                    ApiResponse.Success(status)
                } else {
                    ApiResponse.Error<MovieStatus>(RESPONSE_ERROR)
                }
            }
    }

    override fun getRemoteMovieList(apiKey: String, page: Int): Single<ApiResponse<List<Movie>>>? {
        return service?.getMovieList(apiKey, page)?.map { response ->
            if (response.isSuccessful) {
                val list = response.body()?.movieList ?: emptyList()
                ApiResponse.Success(list)
            } else {
                ApiResponse.Error<List<Movie>>(RESPONSE_ERROR)
            }
        }
    }

    override fun getRemoteFavouriteMovies(
        apiKey: String,
        sessionId: String
    ): Single<ApiResponse<List<Movie>>>? {
        return service?.getFavouriteMovies(apiKey, sessionId)?.map { response ->
            if (response.isSuccessful) {
                val list = response.body()?.movieList ?: emptyList()
                ApiResponse.Success(list)
            } else {
                ApiResponse.Error<List<Movie>>(RESPONSE_ERROR)
            }
        }
    }

    override fun updateRemoteFavouritesRX(
        apiKey: String,
        sessionId: String,
        fav: SelectedMovie
    ): Single<ApiResponse<Boolean>>? {
        return service?.addRemoveFavouritesRX(apiKey, sessionId, fav)
            ?.map { response ->
                if (response.isSuccessful) {
                    ApiResponse.Success(true)
                } else {
                    ApiResponse.Error<Boolean>(RESPONSE_ERROR)
                }
            }
    }


    override fun getLocalSessionId(context: Context): String {
        return if (sharedPreferences.contains(context.getString(R.string.session_id))) {
            sharedPreferences.getString(
                context.getString(R.string.session_id), NULLABLE_VALUE
            ) as String
        } else NULLABLE_VALUE
    }
}

