package com.example.kino.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kino.R
import com.example.kino.model.movie.Movie
import com.example.kino.utils.FragmentEnum
import com.example.kino.utils.constants.*
import com.example.kino.utils.pagination.PaginationScrollListener
import com.example.kino.view.adapters.RecyclerViewAdapter
import com.example.kino.view_model.MoviesListViewModel
import com.example.kino.view_model.SharedViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

class FilmsFragment : Fragment(), RecyclerViewAdapter.RecyclerViewItemClick {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var layoutManager: LinearLayoutManager

    private val recyclerViewAdapter: RecyclerViewAdapter by lazy {
        RecyclerViewAdapter(itemClickListener = this)
    }

    private val moviesListViewModel: MoviesListViewModel by inject()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var currentPage = PaginationScrollListener.PAGE_START
    private var isLocal = false
    private var isLastPage = false
    private var isLoading = false
    private var itemCount = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedViewModel.liked.observe(requireActivity(), Observer { item ->
            recyclerViewAdapter.updateItem(item)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.films_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity())
        bindViews(view)
        setAdapter()
        getMovies(currentPage)
    }

    private fun bindViews(view: View) {
        recyclerView = view.findViewById(R.id.filmsRecyclerView)
        layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            recyclerViewAdapter.clearAll()
            itemCount = 0
            currentPage = PaginationScrollListener.PAGE_START
            isLastPage = false
            getMovies(currentPage)
        }
    }

    private fun setAdapter() {
        recyclerView.adapter = recyclerViewAdapter

        recyclerView.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++
                getMovies(currentPage)
            }

            override fun isLastPage(): Boolean = isLastPage
            override fun isLoading(): Boolean = isLoading
            override fun isLocal(): Boolean = isLocal

        })
    }

    private fun logEvent(logMessage: String, item: Movie) {
        val bundle = Bundle()
        bundle.putString(MOVIE_ID, item.id.toString())
        bundle.putString(MOVIE_TITLE, item.title)
        firebaseAnalytics.logEvent(logMessage, bundle)
    }

    override fun itemClick(position: Int, item: Movie) {
        logEvent(MOVIE_CLICKED, item)
        val bundle = Bundle()
        bundle.putInt(INTENT_KEY, item.id)

        val movieDetailedFragment = MovieDetailsFragment()
        movieDetailedFragment.arguments = bundle

        parentFragmentManager.beginTransaction().add(R.id.frame, movieDetailedFragment)
            .addToBackStack(null).commit()
        requireActivity().toolbar.visibility = View.GONE
        requireActivity().bottomNavigation.visibility = View.GONE
    }

    override fun addToFavourites(position: Int, item: Movie) {
        if (!item.isClicked) logEvent(MOVIE_LIKED, item)
        moviesListViewModel.addToFavourites(item)
        sharedViewModel.setMovie(item)
    }

    private fun getMovies(page: Int) {
        moviesListViewModel.getMovies(FragmentEnum.TOP, page)
        moviesListViewModel.liveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is MoviesListViewModel.State.ShowLoading -> {
                    swipeRefreshLayout.isRefreshing = true
                }
                is MoviesListViewModel.State.HideLoading -> {
                    swipeRefreshLayout.isRefreshing = false
                }
                is MoviesListViewModel.State.Result -> {
                    isLocal = result.isLocal
                    if (result.isLocal) {
                        recyclerViewAdapter.replaceItems(result.moviesList ?: emptyList())
                    } else {
                        recyclerViewAdapter.removeLoading()
                        recyclerViewAdapter.addItems(result.moviesList ?: emptyList())
                        recyclerViewAdapter.addLoading()
                        isLoading = false
                    }
                }
                is MoviesListViewModel.State.Update -> {
                    recyclerViewAdapter.notifyDataSetChanged()
                }
            }
        })
    }
}
