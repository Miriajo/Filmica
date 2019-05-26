package io.keepcoding.filmica.data

import android.arch.persistence.room.Room
import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.keepcoding.filmica.view.films.TAG_FILM
import io.keepcoding.filmica.view.films.TAG_SEARCH
import io.keepcoding.filmica.view.films.TAG_TRENDS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object FilmsRepo {

    private val discoverFilms: MutableList<Film> = mutableListOf()
    private val trendingFilms: MutableList<Film> = mutableListOf()
    private val watchlistFilms: MutableList<Film> = mutableListOf()
    private val searchFilms: MutableList<Film> = mutableListOf()

    @Volatile
    private var db: FilmDatabase? = null

    private fun getDbInstance(context: Context): FilmDatabase {
        if (db == null) {
            db = Room.databaseBuilder(
                context.applicationContext,
                FilmDatabase::class.java,
                "filmica-db"
            ).build()
        }

        return db as FilmDatabase
    }

    fun findFilmById(id: String, type: String): Film? {

        if (type == TAG_FILM) {
            return discoverFilms.find {
                return@find it.id == id
            }
        } else if (type == TAG_TRENDS) {
            return trendingFilms.find {
                return@find it.id == id
            }
        } else if (type == TAG_SEARCH) {
            return searchFilms.find {
                return@find it.id == id
            }
        } else {
            return watchlistFilms.find {
                return@find it.id == id
            }
        }

    }

    fun saveFilm(
        context: Context,
        film: Film,
        callback: (Film) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
            val db = getDbInstance(context)
            db.filmDao().insertFilm(film)
        }

            async.await()
            callback.invoke(film)
        }
    }

    fun getFilms(
    context: Context,
        callback: (List<Film>) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {

            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().getFilms()
            }

            val films = async.await()
            watchlistFilms.clear()
            watchlistFilms.addAll(films)
            callback.invoke(watchlistFilms)
        }
    }


    fun deleteFilm(
        context: Context,
        film: Film,
        callback: (Film) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().deleteFilm(film)
            }

            async.await()
            callback.invoke(film)
        }
    }

    fun discoverFilms(
        context: Context,
        page: Int = 1,
        onResponse: (List<Film>) -> Unit,
        onError: (VolleyError) -> Unit
    ) {

        if (discoverFilms.isEmpty() || (page > 1)) {

            val url = ApiRoutes.discoverMoviesUrl(page = page)
            val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    val films =
                        Film.parseFilms(response.getJSONArray("results"))
                   // FilmsRepo.discoverFilms.clear()
                    FilmsRepo.discoverFilms.addAll(films)
                    onResponse.invoke(FilmsRepo.discoverFilms)
                },
                { error ->
                    error.printStackTrace()
                    onError.invoke(error)
                }
            )

            Volley.newRequestQueue(context)
                .add(request)

        } else {
            onResponse.invoke(discoverFilms)
        }
    }

    // Create a func to get Trendings
    fun trendingFilms(
        context: Context,
        page: Int = 1,
        onResponse: (List<Film>) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        if (trendingFilms.isEmpty() || (page > 1)) {
            val url = ApiRoutes.trendingMoviesUrl(page = page)
            val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    val films =
                        Film.parseFilms(response.getJSONArray("results"))
                    //FilmsRepo.trendingFilms.clear()
                    FilmsRepo.trendingFilms.addAll(films)
                    onResponse.invoke(FilmsRepo.trendingFilms)
                },
                { error ->
                    error.printStackTrace()
                    onError.invoke(error)
                }
            )

            Volley.newRequestQueue(context)
                .add(request)
            } else {
                onResponse.invoke(trendingFilms)
            }
    }


    // Create a func to get Search
    fun searchFilms(
        context: Context,
        query: String,
        onResponse: (List<Film>) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = ApiRoutes.searchMoviesUrl(query)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val films =
                    Film.parseFilms(response.getJSONArray("results"))
                FilmsRepo.searchFilms.clear()
                FilmsRepo.searchFilms.addAll(films)
                onResponse.invoke(FilmsRepo.searchFilms)
            },
            { error ->
                error.printStackTrace()
                onError.invoke(error)
            }
        )

        Volley.newRequestQueue(context)
            .add(request)
    }


    private fun dummyFilms(): MutableList<Film> {
        return (1..10).map { i: Int ->
            return@map Film(
                id = "${i}",
                title = "Film ${i}",
                overview = "Overview ${i}",
                genre = "Genre ${i}",
                rating = i.toFloat(),
                date = "2019-05-${i}"
            )
        }.toMutableList()
    }
}