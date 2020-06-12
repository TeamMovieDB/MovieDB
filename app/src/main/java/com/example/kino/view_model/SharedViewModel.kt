package com.example.kino.view_model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kino.model.movie.Movie

class SharedViewModel: ViewModel() {
    val liked = MutableLiveData<Movie>()

    fun setMovie(movie:Movie){
        liked.value = movie
        Log.d("listtt", "liked")
    }

}