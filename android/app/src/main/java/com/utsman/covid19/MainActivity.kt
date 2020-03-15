package com.utsman.covid19

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import com.utsman.covid19.cluster.CovidCluster
import com.utsman.covid19.cluster.CustomClusterRender
import com.utsman.covid19.ext.*
import com.utsman.covid19.network.Articles
import com.utsman.covid19.network.ItemCluster
import com.utsman.covid19.network.NetworkState
import com.utsman.covid19.network.Total
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import kotlinx.android.synthetic.main.covid_info_window.view.*

class MainActivity : AppCompatActivity() {

    private val composite = CompositeDisposable()
    private var covidCluster: CovidCluster? = null
    private val viewModel: CovidViewModel by viewModels()

    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(bottom_sheet)
    }

    private val infoWindowAdapter = object : GoogleMap.InfoWindowAdapter {

        override fun getInfoContents(marker: Marker): View? {
            return null
        }

        override fun getInfoWindow(marker: Marker?): View? {
            val view = layoutInflater.inflate(R.layout.covid_info_window, null)
            val provinces = covidCluster?.cluster?.data?.provinceOrState
            val country = covidCluster?.cluster?.data?.country
            val title = if (provinces != "Unknown") {
                "$provinces, $country"
            } else {
                country
            }

            view.info_title.text = title
            view.info_confirmed.text = covidCluster?.cluster?.data?.confirmed?.formatted()
            view.info_death.text = covidCluster?.cluster?.data?.death?.formatted()
            view.info_recovered.text = covidCluster?.cluster?.data?.recovered?.formatted()

            bottom_sheet.text_title_report.text = title
            bottom_sheet.text_total.text = covidCluster?.cluster?.data?.confirmed?.formatted()
            bottom_sheet.text_death.text = covidCluster?.cluster?.data?.death?.formatted()
            bottom_sheet.text_recovered.text = covidCluster?.cluster?.data?.recovered?.formatted()

            bottomSheetBehavior.collapse(composite)
            viewModel.getArticles(country)

            setupPieChart(
                Total(
                    confirmed = covidCluster?.cluster?.data?.confirmed ?: 0,
                    death = covidCluster?.cluster?.data?.death ?: 0,
                    recovered = covidCluster?.cluster?.data?.recovered ?: 0
                )
            )
            return view
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeStatusBarTransparent()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_container)) { _, insets ->
            insets.consumeSystemWindowInsets()
        }

        bottomSheetBehavior.hide()
        getTotal()
        //viewModel.getArticles(null)

        viewModel.networkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING, NetworkState.ERROR -> {
                    bottom_sheet.pager_articles.visibility = View.GONE
                    bottom_sheet.text_article.visibility = View.GONE
                }
                else -> {
                    bottom_sheet.pager_articles.visibility = View.VISIBLE
                    bottom_sheet.text_article.visibility = View.VISIBLE
                }
            }
        })

        viewModel.articles.observe(this, Observer {
            addItems(it)
        })

        viewModel.articlesGlobal.observe(this, Observer {
            addItems(it)
        })


        viewModel.getLastDate().observe(this, Observer {
            val day = it.lastDate?.day
            val month = it.lastDate?.month
            val year = it.lastDate?.year
            viewModel.getData(day, month)

            bottom_sheet.text_last_update.text = "Last update: $day/$month/$year"
        })

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottom_sheet.container_main_info.animTo("Y", 25.dpf)
                    bottom_sheet.bottom_card.radius = 0.dpf
                } else {
                    bottom_sheet.container_main_info.animTo("Y", 0.dpf)
                    bottom_sheet.bottom_card.radius = 26.dpf
                }
            }

        })

        val mapsView = (google_map_view as SupportMapFragment)
        mapsView.getMapAsync { gmap ->
            viewModel.getArticles("")

            gmap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_maps))
            gmap.setOnMapClickListener {
                viewModel.total.value?.let {
                    bottom_sheet.text_title_report.text = "Worldwide"
                    bottom_sheet.text_total.text = it.confirmed.formatted()
                    bottom_sheet.text_death.text = it.death.formatted()
                    bottom_sheet.text_recovered.text = it.recovered.formatted()

                    bottomSheetBehavior.collapse(composite)

                    setupPieChart(it)
                }

                viewModel.articlesGlobal.value?.let {
                    addItems(it)
                }
            }

            viewModel.data.observe(this, Observer { data ->
                val clusterManager = ClusterManager<CovidCluster>(this, gmap)
                clusterManager.renderer =
                    CustomClusterRender(
                        this,
                        clusterManager,
                        gmap
                    )

                gmap.setOnCameraIdleListener(clusterManager)
                gmap.setOnMarkerClickListener(clusterManager)

                clusterManager.clusterMarkerCollection.setInfoWindowAdapter(infoWindowAdapter)
                clusterManager.markerCollection.setInfoWindowAdapter(infoWindowAdapter)

                clusterManager.setOnClusterItemClickListener {
                    bottomSheetBehavior.hide()
                    this.covidCluster = it
                    false
                }

                clusterManager.setOnClusterClickListener {
                    val cluster = it.items.random()
                    this.covidCluster = cluster
                    false
                }

                data.forEach {
                    val latLng = LatLng(it.coordinate[0], it.coordinate[1])
                    val itemCluster = ItemCluster(
                        it.confirmed ?: 0,
                        latLng,
                        it
                    )
                    clusterManager.addItem(
                        CovidCluster(
                            itemCluster
                        )
                    )
                    clusterManager.cluster()
                }
            })
        }
    }

    private fun addItems(it: List<Articles>) {
        pager_articles.setPadding(12.dp, 0, 12.dp, 0)
        pager_articles.clipToPadding = false

        val pagerAdapter = PagerAdapter(this)
        pagerAdapter.addArticles(it)

        pager_articles.adapter = pagerAdapter
        pager_articles.offscreenPageLimit = 20
    }

    private fun getTotal() {
        viewModel.total.observe(this, Observer {
            bottom_sheet.text_title_report.text = "Worldwide"
            bottom_sheet.text_total.text = it.confirmed.formatted()
            bottom_sheet.text_death.text = it.death.formatted()
            bottom_sheet.text_recovered.text = it.recovered.formatted()

            bottomSheetBehavior.collapse(composite)
            setupPieChart(it)
        })

    }

    private fun setupPieChart(total: Total?) {
        val valueFormat = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().formatted()
            }
        }

        val confirmedValue = (total?.confirmed ?: 0).toFloat()
        val deathValue = (total?.death ?: 0).toFloat()
        val recoveredValue = (total?.recovered ?: 0).toFloat()

        val confirmedColor = ContextCompat.getColor(this, R.color.colorConfirmed)
        val deathColor = ContextCompat.getColor(this, R.color.colorDeath)
        val recoveredColor = ContextCompat.getColor(this, R.color.colorRecovered)

        val rawData = listOf(
            PieEntry(confirmedValue, "Confirmed"),
            PieEntry(deathValue, "Death"),
            PieEntry(recoveredValue, "Recovered")
        )

        val pieDataSet = PieDataSet(rawData, "")
        pieDataSet.valueTextColor = Color.WHITE
        pieDataSet.valueTextSize = 10f
        pieDataSet.valueFormatter = valueFormat

        val pieData = PieData(pieDataSet)
        pieData.setValueTextColor(Color.WHITE)
        pieDataSet.setColors(confirmedColor, deathColor, recoveredColor)

        pie_chart.setEntryLabelTextSize(10f)
        pie_chart.setHoleColor(Color.TRANSPARENT)
        pie_chart.setEntryLabelColor(ContextCompat.getColor(this, R.color.colorSubtitle))
        pie_chart.legend.textColor = ContextCompat.getColor(this, R.color.colorSubtitle)
        pie_chart.description.text = ""
        pie_chart.description.textColor = ContextCompat.getColor(this, R.color.colorSubtitle)

        pie_chart.data = pieData
        pie_chart.invalidate()

    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.isCollapse()) {
            super.onBackPressed()
        } else {
            bottomSheetBehavior.collapse(composite)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        composite.dispose()
    }
}
