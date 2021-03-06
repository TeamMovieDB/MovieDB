package com.example.kino.view_model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.kino.model.movie.Movie
import com.example.kino.model.movie.MovieStatus
import com.example.kino.model.movie.SelectedMovie
import com.example.kino.model.repository.MovieRepository
import com.example.kino.utils.constants.API_KEY
import com.example.kino.utils.constants.MEDIA_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MovieDetailsViewModel(
    context: Context,
    private val movieRepository: MovieRepository
) : BaseViewModel() {

    private var sessionId = movieRepository.getLocalSessionId(context)
    val liveData = MutableLiveData<State>()

    fun getMovie(id: Int) {

        launch {
            val movie = withContext(Dispatchers.IO) {
                try {
                    val movieDetails = movieRepository.getRemoteMovie(
                        id,
                        API_KEY
                    )
                    val movieState = movieRepository.getRemoteMovieStates(
                        id,
                        API_KEY, sessionId
                    )
                    if (movieDetails != null) {
                        if (movieState != null) {
                            movieDetails.isClicked = movieState
                        }
                        setGenres(movieDetails)
                        movieDetails.tagline?.let {
                            movieDetails.runtime?.let { it1 ->
                                movieRepository.updateLocalMovieProperties(
                                    it,
                                    it1, movieDetails.id
                                )
                            }
                        }
                    }
                    return@withContext movieDetails

                } catch (e: Exception) {
                    movieRepository.getLocalMovie(id)
                }
            }
            liveData.value = State.HideLoading
            liveData.value = State.Result(movie)
        }
    }

    fun updateLikeStatus(item: Movie) {
        val movie = SelectedMovie(MEDIA_TYPE, item.id, item.isClicked)
        launch {
            withContext(Dispatchers.IO) {
                try {
                    movieRepository.updateRemoteFavourites(API_KEY, sessionId, movie)
                    movieRepository.updateLocalMovieIsCLicked(movie.selectedStatus, movie.movieId)

                } catch (e: Exception) {
                    movieRepository.updateLocalMovieIsCLicked(item.isClicked, item.id)
                    movieRepository.insertLocalMovieStatus(
                        MovieStatus(movie.movieId, movie.selectedStatus)
                    )
                }
            }
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
