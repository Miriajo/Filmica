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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_films)

        if (savedInstanceState == null) {
            setupFragments()
        } else {
            val tag = savedInstanceState.getString("active", TAG_FILM)
            restoreFragments(tag)
        }

        // Show the correct fragment taking into account the menu button clicked
        navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_discover -> showMainFragment(filmsFragment)
                R.id.action_trending -> showMainFragment(trendsFragment)
                R.id.action_watchlist -> showMainFragment(watchlistFragment)
                R.id.action_search -> showMainFragment(searchFragment)
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

    private fun restoreFragments(tag: String) {
        filmsFragment = supportFragmentManager.findFragmentByTag(TAG_FILM) as FilmsFragment
        trendsFragment = supportFragmentManager.findFragmentByTag(TAG_TRENDS) as TrendsFragment
        watchlistFragment =
            supportFragmentManager.findFragmentByTag(TAG_WATCHLIST) as WatchlistFragment
        searchFragment =
                supportFragmentManager.findFragmentByTag(TAG_SEARCH) as SearchFragment

        when (tag) {
            TAG_TRENDS -> activeFragment = trendsFragment
            TAG_WATCHLIST -> activeFragment = watchlistFragment
            TAG_SEARCH -> activeFragment = searchFragment
            else -> activeFragment = filmsFragment
        }

    }

    private fun showMainFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()

        activeFragment = fragment
    }

    override fun onClick(film: Film) {
        if (!isDetailDetailViewAvailable()) {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("id", film.id)
            startActivity(intent)
        } else {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container_detail,
                    DetailFragment.newInstance(film.id)
                )
                .commit()
        }
    }

    private fun isDetailDetailViewAvailable() =
        findViewById<FrameLayout>(R.id.container_detail) != null
}