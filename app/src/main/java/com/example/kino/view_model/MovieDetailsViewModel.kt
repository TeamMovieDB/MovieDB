package com.example.kino.view_model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.kino.model.movie.Movie
import com.example.kino.model.movie.MovieStatus
import com.example.kino.model.movie.SelectedMovie
import com.example.kino.model.repository.MovieRepository
import com.example.kino.utils.ApiResponse
import com.example.kino.utils.constants.API_KEY
import com.example.kino.utils.constants.MEDIA_TYPE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class MovieDetailsViewModel(
    context: Context,
    private val movieRepository: MovieRepository
) : BaseViewModel() {

    private var sessionId = movieRepository.getLocalSessionId(context)
    val liveData = MutableLiveData<State>()

    fun getMovie(id: Int) {
        movieRepository.getRemoteMovie(id, API_KEY)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { result ->
                    when (result) {
                        is ApiResponse.Success<Movie> -> {
                            val movie = result.result
                            setMovieState(movie)
                            setGenres(movie)
//                            movie.tagline?.let {
//                                movie.runtime?.let { it1 ->
//                                    movieRepository.updateLocalMovieProperties(
//                                        it,
//                                        it1,
//                                        movie.id
//                                    )
//                                }
//                            }
                        }
                        is ApiResponse.Error -> {
                            liveData.value = State.HideLoading
//                            val movie = movieRepository.getLocalMovie(id)
//                            liveData.value = State.HideLoading
//                            liveData.value = State.Result(movie)
                        }
                    }
                },
                {
                    liveData.value = State.HideLoading
//                    val movie = movieRepository.getLocalMovie(id)
//                    liveData.value = State.HideLoading
//                    liveData.value = State.Result(movie)
                }
            )?.let {
                disposable.add(
                    it
                )
            }
    }

    private fun setMovieState(movie: Movie) {
        movieRepository.getRemoteMovieStateRX(movie.id, API_KEY, sessionId)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { result ->
                    when (result) {
                        is ApiResponse.Success<MovieStatus> -> {
                            movie.isClicked = result.result.selectedStatus
                            liveData.value = State.HideLoading
                            liveData.value = State.Result(movie)
                        }
                        is ApiResponse.Error -> {
                            liveData.value = State.HideLoading
                            liveData.value = State.Result(movie)
                        }
                    }
                }, {
                    liveData.value = State.HideLoading
                    liveData.value = State.Result(movie)
                }
            )?.let { disposable.add(it) }
    }

    fun updateLikeStatus(item: Movie) {
        val movie = SelectedMovie(MEDIA_TYPE, item.id, item.isClicked)

        //      movieRepository.updateLocalMovieIsCLicked(movie.selectedStatus, movie.movieId)

        movieRepository.updateRemoteFavouritesRX(API_KEY, sessionId, movie)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { result ->
                    when (result) {
                        is ApiResponse.Error -> {
//                            movieRepository.updateLocalMovieIsCLicked(item.isClicked, item.id)
//                            movieRepository.insertLocalMovieStatus(
//                                MovieStatus(movie.movieId, movie.selectedStatus)
//                            )
                        }
                    }
                },
                {
//                    movieRepository.updateLocalMovieIsCLicked(item.isClicked, item.id)
//                    movieRepository.insertLocalMovieStatus(
//                        MovieStatus(movie.movieId, movie.selectedStatus)
//                    )
                }

            )?.let {
                disposable.add(
                    it
                )
            }
    }

    private fun setGenres(movie: Movie) {
        movie.genreNames = ""
        if (movie.genres != null) {
            for (i in movie.genres.indices) {
                if (i == 0) movie.genreNames += movie.genres[i].genre.toLowerCase(Locale.ROOT)
                else movie.genreNames += ", " + movie.genres[i].genre.toLowerCase(Locale.ROOT)
            }
        } else {
            movie.genreNames = movie.genreNames.substring(0, movie.genreNames.length)
        }
    }

    sealed class State {
        object HideLoading : State()
        data class Result(val movie: Movie?) : State()
    }
}
