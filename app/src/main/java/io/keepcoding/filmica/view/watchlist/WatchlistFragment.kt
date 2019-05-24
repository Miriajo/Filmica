package io.keepcoding.filmica.view.watchlist


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import io.keepcoding.filmica.R
import io.keepcoding.filmica.data.Film
import io.keepcoding.filmica.data.FilmsRepo
import io.keepcoding.filmica.view.films.FilmsFragment
import io.keepcoding.filmica.view.util.BaseFilmHolder
import io.keepcoding.filmica.view.util.SwipeToDeleteCallback
import kotlinx.android.synthetic.main.activity_films.*
import kotlinx.android.synthetic.main.fragment_watchlist.*

class WatchlistFragment : Fragment() {


    lateinit var listener: FilmsFragment.OnFilmClickLister

    val adapter: WatchListAdapter = WatchListAdapter {
        listener.onClick(it)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is FilmsFragment.OnFilmClickLister) {
            listener = context
        } else {
            throw IllegalArgumentException("The attached activity isn't implementing ${FilmsFragment.OnFilmClickLister::class.java.canonicalName}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeHandler()
        watchlist.adapter = adapter
    }


    private fun setupSwipeHandler() {
        val swipeHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                val film = (holder as BaseFilmHolder).film
                val position = holder.adapterPosition
                deleteFilm(film, position)

            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(watchlist)
    }

    private fun deleteFilm(film: Film, position: Int) {
        FilmsRepo.deleteFilm(context!!, film) {
            adapter.deleteFilm(position)

            Snackbar.make(watchlist, R.string.remove_watchlist, Snackbar.LENGTH_LONG)
                    .setAction("UNDO"){
                        FilmsRepo.saveFilm(context!!, film) {
                            adapter.insertFilm(position, film)
                        }
                    }
                    .setActionTextColor(Color.WHITE)
                    .show()
        }

    }

    override fun onResume() {
        super.onResume()

        FilmsRepo.getFilms(context!!) {
            adapter.setFilms(it)
        }
    }

    interface OnFilmClickLister {
        fun onClick(film: Film)
    }

}
