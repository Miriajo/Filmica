package io.keepcoding.filmica.view.search

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.keepcoding.filmica.R
import io.keepcoding.filmica.data.Film
import io.keepcoding.filmica.data.FilmsRepo
import io.keepcoding.filmica.view.films.FilmsAdapter
import io.keepcoding.filmica.view.films.FilmsFragment
import io.keepcoding.filmica.view.util.GridOffsetDecoration
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.layout_error_search.view.*

class SearchFragment : Fragment() {

    lateinit var listener: FilmsFragment.OnFilmClickLister

    val list: RecyclerView by lazy {
        listSearch.addItemDecoration(GridOffsetDecoration())
        return@lazy listSearch
    }

    val adapter = FilmsAdapter {
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
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.adapter = adapter
        btnSearch.setOnClickListener { reload() }
        showDefault()
    }

    override fun onResume() {
        super.onResume()
        if (txtSearch.text.toString().isEmpty()) {
            showDefault()
        } else
            reload()
    }

    private fun reload() {
        showProgress()

        val query = txtSearch.text.toString()

        if (query.length < 3 ) {
            showError(R.string.ERROR_CHARS.toString())
        }
        else {

            FilmsRepo.searchFilms(context!!,
                txtSearch.text.toString(),
                { films ->
                    adapter.setFilms(films)
                    if (films.size == 0) {
                        showError(R.string.ERROR_EMPTY.toString())
                    } else {
                        showList()
                    }

                }, { errorRequest ->
                    showError(R.string.ERROR_WRONG.toString())
                })
        }

    }


    private fun showList() {
        searchProgress.visibility = View.INVISIBLE
        errorSearch.visibility = View.INVISIBLE
        list.visibility = View.VISIBLE
    }

    private fun showError(error: String) {
        searchProgress.visibility = View.INVISIBLE
        list.visibility = View.INVISIBLE
        errorSearch.txtError.text = error
        errorSearch.visibility = View.VISIBLE
    }

    private fun showProgress() {
        searchProgress.visibility = View.VISIBLE
        errorSearch.visibility = View.INVISIBLE
        list.visibility = View.INVISIBLE
    }

    private fun showDefault() {
        searchProgress.visibility = View.INVISIBLE
        errorSearch.visibility = View.INVISIBLE
        list.visibility = View.INVISIBLE
    }

    interface OnFilmClickLister {
        fun onClick(film: Film)
    }
}