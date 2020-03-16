package com.utsman.covid19

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.utsman.covid19.network.Articles
import com.utsman.covid19.network.RetrofitInstance
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_articles.view.*

class PagerAdapter(private val context: Context) : PagerAdapter() {
    private val items: MutableList<Articles> = mutableListOf()

    fun addArticles(items: List<Articles>) {
        this.items.clear()
        notifyDataSetChanged()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    @SuppressLint("CheckResult")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_articles, container, false)
        val articles = items[position]
        view.text_title_articles.text = articles.title

        RetrofitInstance.create().getUrlThumbnail(articles.url)
            .subscribeOn(Schedulers.io())
            .map { it.imageUrl }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Glide.with(context).load(it).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(view.image_articles)
            }, {
               it.printStackTrace()
            })

        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
        val customTab = builder.build()

        view.setOnClickListener {
            customTab.launchUrl(context, Uri.parse(articles.url))
        }

        container.addView(view)

        return view
    }

    override fun getCount(): Int = items.size

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}