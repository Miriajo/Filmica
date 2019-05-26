package io.keepcoding.filmica.view.films

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import io.keepcoding.filmica.R
import io.keepcoding.filmica.data.Film
import io.keepcoding.filmica.view.detail.DetailActivity
import io.keepcoding.filmica.view.detail.DetailFragment
import io.keepcoding.filmica.view.detail.PlaceholderFragment
import io.keepcoding.filmica.view.search.SearchFragment
import io.keepcoding.filmica.view.watchlist.WatchlistFragment
import kotlinx.android.synthetic.main.activity_films.*

const val TAG_FILM = "films"
const val TAG_TRENDS = "trending"
const val TAG_WATCHLIST = "watchlist"
const val TAG_SEARCH = "search"

class FilmsActivity : AppCompatActivity(),
    FilmsFragment.OnFilmClickLister,
    TrendsFragment.OnFilmClickLister,
    SearchFragment.OnFilmClickLister,
    WatchlistFragment.OnFilmClickLister {

    private lateinit var filmsFragment: FilmsFragment
    private lateinit var trendsFragment: TrendsFragment
    private lateinit var watchlistFragment: WatchlistFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var activeFragment: Fragment

    private var detailSelected: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_films)

        if (savedInstanceState == null) {
            if (isDetailViewAvailable())
                showPlaceholder()

            setupFragments()
        } else {
            if (isDetailViewAvailable() and detailSelected)
                showPlaceholder()

            val activeTag = savedInstanceState.getString("active", TAG_FILM)
            restoreFragments(activeTag)

        }

        // Show the correct fragment taking into account the menu button clicked
        navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_discover -> showMainFragment(filmsFragment, false)
                R.id.action_trending -> showMainFragment(trendsFragment, false)
                R.id.action_watchlist -> showMainFragment(watchlistFragment, true)
                R.id.action_search -> showMainFragment(searchFragment, false)
            }

            true
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("active", activeFragment.tag)
    }

    private fun setupFragments() {
        filmsFragment = FilmsFragment()
        trendsFragment = TrendsFragment()
        watchlistFragment = WatchlistFragment()
        searchFragment = SearchFragment()
        activeFragment = filmsFragment

        supportFragmentManager.beginTransaction()
            .add(R.id.container, filmsFragment, TAG_FILM)
            .add(R.id.container, trendsFragment, TAG_TRENDS)
            .add(R.id.container, watchlistFragment, TAG_WATCHLIST)
            .add(R.id.container, searchFragment, TAG_SEARCH)
            .hide(trendsFragment)
            .hide(watchlistFragment)
            .hide(searchFragment)
            .commit()
    }

    private fun restoreFragments(activeTag: String) {
        filmsFragment = supportFragmentManager.findFragmentByTag(TAG_FILM) as FilmsFragment
        trendsFragment = supportFragmentManager.findFragmentByTag(TAG_TRENDS) as TrendsFragment
        watchlistFragment =
            supportFragmentManager.findFragmentByTag(TAG_WATCHLIST) as WatchlistFragment
        searchFragment =
                supportFragmentManager.findFragmentByTag(TAG_SEARCH) as SearchFragment

        activeFragment = when (activeTag) {
            TAG_TRENDS -> trendsFragment
            TAG_WATCHLIST -> watchlistFragment
            TAG_SEARCH -> searchFragment
            else -> filmsFragment
        }

    }

    private fun showMainFragment(fragment: Fragment, isWatchlist: Boolean) {

        if (isDetailViewAvailable()) {
            showPlaceholder()
        }

        if (isWatchlist) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .detach(fragment)
                .attach(fragment)
                .show(fragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(fragment)
                .commit()
        }

        activeFragment = fragment
    }

    private fun showPlaceholder() {
        val fragment = PlaceholderFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container_detail, fragment)
            .commit()

        detailSelected = false
    }

    override fun onClick(film: Film) {
        if (!isDetailViewAvailable()) {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("id", film.id)
            intent.putExtra("filmType", activeFragment.tag.toString())
            startActivity(intent)

            detailSelected = false

        } else {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container_detail,
                    DetailFragment.newInstance(film.id, activeFragment.tag.toString())
                )
                .commit()

            detailSelected = true
        }
    }

    private fun isDetailViewAvailable() =
        findViewById<FrameLayout>(R.id.container_detail) != null
}