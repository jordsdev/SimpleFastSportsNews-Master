package com.example.simplefastnews

import android.app.SearchManager
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.simplefastnews.adapter.ArticleAdapter
import com.example.simplefastnews.model.Article
import com.example.simplefastnews.model.TopSportHeadlines
import com.example.simplefastnews.sportapi.SportHeadlinesApi
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_article.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Url
import java.lang.UnsupportedOperationException

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    //Url of the News API
    private val ENDPOINT_URL by lazy { "https://newsapi.org/v2/"}
    private lateinit var sportHeadlinesApi: SportHeadlinesApi
    private lateinit var newsApiConfig: String
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleList: ArrayList<Article>
    private lateinit var userKeyWordInput: String
    private lateinit var topSportHeadlinesObservable: Observable<TopSportHeadlines>
    private lateinit var compositeDisposable: CompositeDisposable

    //onCreate set the content view.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Retrofit parameters and API key
        val retrofit: Retrofit = generateRetrofitBuilder()
        sportHeadlinesApi = retrofit.create(SportHeadlinesApi::class.java)

        //Call for the Api 
        newsApiConfig = resources.getString(R.string.api_key)
        swipe_refresh.setOnRefreshListener(this)
        swipe_refresh.setColorSchemeResources(R.color.colorBlack)
        articleList = ArrayList()
        articleAdapter = ArticleAdapter(articleList)


        //Sets the user keyword input to start at nothing
        userKeyWordInput = ""

        compositeDisposable = CompositeDisposable()
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.itemAnimator = DefaultItemAnimator()
        recycler_view.adapter = articleAdapter
    }

    //onStart  check the users keyword input
    override fun onStart() {
        super.onStart()
        checkUserKeywordInput()
    }

    //onDestroy clear the compositeDisposable
    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    //onRefresh check the users keyword input
    override fun onRefresh() {
        checkUserKeywordInput()
    }

    //Check the users keyword input
    private fun checkUserKeywordInput() {
        if (userKeyWordInput.isEmpty()) {
            queryTopSportHeadlines()
        } else {
            getKeyWordQuery(userKeyWordInput)
        }
    }

    //onCreate options menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            val inflater = menuInflater
            inflater.inflate(R.menu.menu_main, menu)
            //Creates input field for the user search
            setUpSearchMenuItem(menu)
        }
        return true
    }

    //Setup the Search menu item
    private fun setUpSearchMenuItem(menu: Menu) {
        val searchManager: SearchManager =
                (getSystemService(Context.SEARCH_SERVICE)) as SearchManager
        val searchView: SearchView = ((menu.findItem(R.id.action_search)?.actionView)) as SearchView
        val searchMenuItem: MenuItem = menu.findItem(R.id.action_search)

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = "Type any keyword to search..."
        searchView.setOnQueryTextListener(onQueryTextListenerCallback())
        searchMenuItem.icon.setVisible(false, false)
    }

    //onQuery text listener for the search view
    private fun onQueryTextListenerCallback(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(userInput: String?): Boolean {
                return checkQueryText(userInput)
            }

            //onQuery text change user input
            override fun onQueryTextChange(userInput: String?): Boolean {
                return checkQueryText(userInput)
            }
        }
    }

    //Query the Api by use of user input
    private fun getKeyWordQuery(userKeywordInput: String) {
        swipe_refresh.isRefreshing = true
        if (userKeywordInput != null && userKeywordInput.isNotEmpty()) {
            topSportHeadlinesObservable =
                sportHeadlinesApi.getUserSearchInput(newsApiConfig, userKeywordInput)
            subscribeObservableOfArticle()
        } else {
            queryTopSportHeadlines()
        }
    }

    //Check the query text input
    private fun checkQueryText(userInput: String?): Boolean {
        if (userInput != null && userInput.length > 1) {
            userKeyWordInput = userInput
            getKeyWordQuery(userInput)
        } else if (userInput != null && userInput == "") {
            userKeyWordInput = ""
            queryTopSportHeadlines()
        }
        return false
    }

    //Query the API for the normal articles
    private fun queryTopSportHeadlines() {
        swipe_refresh.isRefreshing = true
        topSportHeadlinesObservable = sportHeadlinesApi.getTopHeadlines("sport", "gb", newsApiConfig)
        subscribeObservableOfArticle()
    }

    //Subscribe Observable Article
    private fun subscribeObservableOfArticle() {
        articleList.clear()
        compositeDisposable.add(
            topSportHeadlinesObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { Observable.fromIterable(it.articles) }
                .subscribeWith(createArticleObserver())
        )
    }

    //Article observer and Disposable Observer
    private fun createArticleObserver(): DisposableObserver<Article> {
        return object : DisposableObserver<Article>() {
            override fun onNext(article: Article) {
                if (!articleList.contains(article)) {
                    articleList.add(article)
                }
            }

            //onComplete RecyclerView show articles
            override fun onComplete() {
                showArticlesOnRecyclerView()
            }

            //Give article error
            override fun onError(e: Throwable) {
                Log.e("createArticleObserver", "Article error: ${e.message}")
            }
        }
    }

    //Show the articles on RecyclerView
    private fun showArticlesOnRecyclerView() {
        if (articleList.size > 0) {
            empty_text.visibility = View.GONE
            retry_fetch_button.visibility = View.GONE
            recycler_view.visibility = View.VISIBLE
            articleAdapter.setArticles(articleList)
        } else {
            recycler_view.visibility = View.GONE
            empty_text.visibility = View.VISIBLE
            retry_fetch_button.visibility = View.VISIBLE
            retry_fetch_button.setOnClickListener { checkUserKeywordInput() }
        }
        swipe_refresh.isRefreshing = false
    }

    //Retrofit Builder and generator.
    private fun generateRetrofitBuilder(): Retrofit {

        val cacheSize: Long = 10 * 1024 * 1024 // 10 MB

        val cache = Cache(cacheDir, cacheSize)

        val okHttpClient = OkHttpClient.Builder()
                .cache(cache)
                .build()

        return Retrofit.Builder()
                .baseUrl(ENDPOINT_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }
}


