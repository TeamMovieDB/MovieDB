package com.example.kino.utils

import com.example.kino.model.account.LoginValidationData
import com.example.kino.model.account.Session
import com.example.kino.model.account.Success
import com.example.kino.model.account.Token
import com.example.kino.model.movie.*
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface PostApi {

    @GET("movie/top_rated")
    fun getMovieList(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): Single<Response<Movies>>

    @GET("movie/{id}")
    fun getMovieById(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): Single<Response<Movie>>

    @GET("account/{account_id}/favorite/movies")
    fun getFavouriteMovies(
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String
    ): Single<Response<Movies>>

    //replace with RX
    @POST("account/{account_id}/favorite")
    suspend fun addRemoveFavourites(
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
        @Body fav: SelectedMovie
    ): Response<StatusResponse>

    @POST("account/{account_id}/favorite")
    fun addRemoveFavouritesRX(
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String,
        @Body fav: SelectedMovie
    ): Single<Response<StatusResponse>>

    //replace with RX
    @GET("movie/{movie_id}/account_states")
    suspend fun getMovieStates(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String
    ): Response<MovieStatus>

    @GET("movie/{movie_id}/account_states")
    fun getMovieStatesRX(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("session_id") sessionId: String
    ): Single<Response<MovieStatus>>

    @GET("genre/movie/list")
    suspend fun getGenres(@Query("api_key") apiKey: String): Response<Genres>

    @GET("authentication/token/new")
    fun createRequestToken(@Query("api_key") apiKey: String): Single<Response<Token>>

    @POST("authentication/token/validate_with_login")
    fun validateWithLogin(
        @Query("api_key") apiKey: String,
        @Body data: LoginValidationData
    ): Single<Response<Token>>

    @POST("authentication/session/new")
    fun createSession(
        @Query("api_key") apiKey: String,
        @Body token: Token
    ): Single<Response<Session>>

    @HTTP(method = "DELETE", path = "authentication/session", hasBody = true)
    fun deleteSession(
        @Query("api_key") apiKey: String,
        @Body session: Session
    ): Single<Response<Success>>
}
