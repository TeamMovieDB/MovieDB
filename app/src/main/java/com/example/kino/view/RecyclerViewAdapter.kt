package com.example.kino.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kino.R
import com.example.kino.model.movie.Movie
import com.squareup.picasso.Picasso

class RecyclerViewAdapter(
    //var movies: List<Movie>? = null,
    val itemClickListener: RecyclerViewItemClick? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var moviePosition = 1
    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_NORMAL = 1

    private var isLoaderVisible = false
    private var movies = mutableListOf<Movie>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_NORMAL -> MovieViewHolder(
                inflater.inflate(R.layout.film_object, parent, false)
            )
            VIEW_TYPE_LOADING -> ProgressViewHolder(
                inflater.inflate(R.layout.layout_progress, parent, false)
            )
            else -> throw Throwable("invalid view")
        }
    }

    override fun getItemCount(): Int = movies.size

    override fun getItemViewType(position: Int): Int {
        return if (isLoaderVisible) {
            if (position == movies.size - 1) {
                VIEW_TYPE_LOADING
            } else {
                VIEW_TYPE_NORMAL
            }
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MovieViewHolder) {
            holder.bind(movies[position])
        }
    }

//    fun replaceItems(list: List<Movie>) {
//        movies.addAll(list)
//        notifyDataSetChanged()
//    }

    fun clearAll() {
        moviePosition = 1
        movies.clear()
        notifyDataSetChanged()
    }

    fun addLoading() {
        isLoaderVisible = true
        movies.add(Movie(id = -1))
        notifyItemInserted(movies.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false
        val position = movies.size - 1
        if (movies.isNotEmpty()) {
            val item = getItem(position)
            if (item != null) {
                movies.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    private fun getItem(position: Int): Movie? {
        return movies[position]
    }

    fun replaceItems(moviesList: List<Movie>) {
        if (movies.isNullOrEmpty()) movies = moviesList as MutableList<Movie>
        else {
            if (movies[movies.size - 1] != moviesList[moviesList.size - 1])
                movies.addAll(moviesList)
        }
        //     Log.d("listtt", "added list " + moviesList.size.toString() + " " + moviesList[0].title)
        //    Log.d("listtt", ("observed list " + (movies as MutableList<Movie>).size.toString()))
        notifyDataSetChanged()
    }

    inner class MovieViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(movie: Movie?) {

            val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            val tvReleaseDate = view.findViewById<TextView>(R.id.tvReleaseDate)
            val tvGenres = view.findViewById<TextView>(R.id.tvGenres)
            val poster = view.findViewById<ImageView>(R.id.ivPoster)
            val tvVotesCount = view.findViewById<TextView>(R.id.tvVotesCount)
            val tvRating = view.findViewById<TextView>(R.id.tvRating)
            val number = view.findViewById<TextView>(R.id.number)
            val addToFav = view.findViewById<ImageView>(R.id.tvAddToFav)

            if (movie != null) {
                if (movie.isClicked) {
                    addToFav.setImageResource(R.drawable.ic_turned_in_black_24dp)
                } else {
                    addToFav.setImageResource(R.drawable.ic_turned_in_not_black_24dp)
                }


                if (movie.position == 0) {
                    movie.position = moviePosition
                    moviePosition++
                }

                tvTitle.text = movie.title

                tvReleaseDate.text = movie.releaseDate.substring(0, 4)
                tvVotesCount.text = movie.voteCount.toString()
                tvRating.text = movie.voteAverage.toString()
                number.text = movie.position.toString()
                tvGenres.text = movie.genreNames.substring(0, movie.genreNames.length - 2)

                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w500" + movie.posterPath)
                    .into(poster)

                view.setOnClickListener {
                    itemClickListener?.itemClick(adapterPosition, movie)
                }

                addToFav.setOnClickListener {
                    itemClickListener?.addToFavourites(adapterPosition, movie)
                    if (movie.isClicked) {
                        addToFav.setImageResource(R.drawable.ic_turned_in_black_24dp)
                    } else {
                        addToFav.setImageResource(R.drawable.ic_turned_in_not_black_24dp)
                    }
                }
            }
        }
    }

    inner class ProgressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }


    interface RecyclerViewItemClick {
        fun itemClick(position: Int, item: Movie)
        fun addToFavourites(position: Int, item: Movie)
    }
}