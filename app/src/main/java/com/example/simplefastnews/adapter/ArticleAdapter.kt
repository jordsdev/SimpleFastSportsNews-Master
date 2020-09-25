package com.example.simplefastnews.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplefastnews.R
import com.example.simplefastnews.ScrollingActivity
import com.example.simplefastnews.model.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item.view.*


//Main Class
class ArticleAdapter(
    private var articleList: ArrayList<Article>
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    //Random picture that's 150 x 150 and its a placeholder for the images.
    private val placeHolderImage = "https://www.google.com/imgres?imgurl=https%3A%2F%2Fimages-wixmp-ed30a86b8c4ca887773594c2.wixmp.com%2Ff%2F5a59d3ac-0919-4001-b19f-e5d5787e8ee8%2Fd8xo72u-720e48f5-e268-406e-b094-3e7faa116ca3.png%3Ftoken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcLzVhNTlkM2FjLTA5MTktNDAwMS1iMTlmLWU1ZDU3ODdlOGVlOFwvZDh4bzcydS03MjBlNDhmNS1lMjY4LTQwNmUtYjA5NC0zZTdmYWExMTZjYTMucG5nIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.1_6J67MFdLRQFRD8JhDGHNqsxEsQTteRqHo7tEyjMd4&imgrefurl=https%3A%2F%2Fwww.deviantart.com%2Fkr151%2Fart%2FLFC-Crest-Avatar-540285654&docid=eXhpyzlN3un2FM&tbnid=qp9cTtJhiAituM%3A&vet=10ahUKEwid9MfHkYfkAhVoShUIHd7WDuoQMwhNKAgwCA..i&w=512&h=512&client=safari&bih=645&biw=1280&q=512x%20512%20image%20lfc&ved=0ahUKEwid9MfHkYfkAhVoShUIHd7WDuoQMwhNKAgwCA&iact=mrc&uact=8"
    private lateinit var viewGroupContext: Context

    //Create the ViewHolder
    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ArticleViewHolder {
        viewGroupContext = viewGroup.context
        val itemView: View = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.item,
            viewGroup,
            false
        )
        return ArticleViewHolder(itemView)
    }

    //Count the Article Items Count
    override fun getItemCount(): Int {
        return articleList.size
    }

    //Bind the ViewHolder and do the onCLickListener
    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, itemIndex: Int) {
        val article: Article = articleList.get(itemIndex)
        setPropertiesForArticleViewHolder(articleViewHolder, article)
        articleViewHolder.cardView.article_imagefromUrl.setOnClickListener {
            viewGroupContext.startActivity(Intent(viewGroupContext, ScrollingActivity::class.java).putExtra("URL", article.url))
        }
    }

    //Set the properties for the ArticleViewHolder
    private fun setPropertiesForArticleViewHolder(
        articleViewHolder: ArticleViewHolder,
        article: Article
    ) {
        checkForUrlToImage(article, articleViewHolder)
        articleViewHolder.title.text = article.title
        articleViewHolder.description.text = article.description
        articleViewHolder.source.text = article.source.name

    }

    //Picasso settings for the Url to Image. Parameters are set to center the image in the
    private fun checkForUrlToImage(article: Article, articleViewHolder: ArticleViewHolder) {
        if (article.urlToImage == null || article.urlToImage.isEmpty()) {
            Picasso.get()
                .load(placeHolderImage)
                .centerCrop()
                .fit()
                .into(articleViewHolder.urlToImage)
        } else {
            Picasso.get()
                .load(article.urlToImage)
                .centerCrop()
                .fit()
                .into(articleViewHolder.urlToImage)
        }
    }

    //Sets the articles.
    fun setArticles(articles: ArrayList<Article>) {
        articleList = articles
        notifyDataSetChanged()
    }

    //ArticleViewHolder properties. Adds the properties for each.
    inner class ArticleViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        val cardView: CardView by lazy { view.card_view }
        val urlToImage: ImageView by lazy { view.article_imagefromUrl }
        val title: TextView by lazy { view.article_title }
        val description: TextView by lazy { view.article_description }
        val source: TextView by lazy { view.article_source }

    }
}