package com.utsman.covid19

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.DimenHolder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.utsman.covid19.cluster.CovidCluster
import com.utsman.covid19.cluster.CustomClusterRender
import com.utsman.covid19.ext.*
import com.utsman.covid19.network.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import kotlinx.android.synthetic.main.covid_info_window.view.*
import kotlinx.android.synthetic.main.header_drawer.view.*

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

            viewModel.getTimeLine(country).observe(this@MainActivity, Observer {
                setupLineChart(it?.timeLine)
            })

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

        setupDrawer()
        bottomSheetBehavior.hide()
        getTotal()
        main_progress_bar.setMarginTop(37.dp)

        viewModel.networkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> {
                    bottomSheetBehavior.hide()
                    main_progress_bar.visibility = View.VISIBLE
                    bottom_sheet.pager_articles.visibility = View.GONE
                    bottom_sheet.text_article.visibility = View.GONE
                }
                NetworkState.ERROR -> {
                    main_progress_bar.visibility = View.GONE
                    bottom_sheet.pager_articles.visibility = View.GONE
                    bottom_sheet.text_article.visibility = View.GONE
                }
                else -> {
                    main_progress_bar.visibility = View.GONE
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

        viewModel.getTimeLine("").observe(this, Observer {
            setupLineChart(it?.timeLine)
        })

        viewModel.getLastDate().observe(this, Observer {
            val day = it.lastDate?.day
            val month = it.lastDate?.month
            val year = it.lastDate?.year
            viewModel.getData(day, month)

            bottom_sheet.text_last_update.text = "Last update: $day/$month/$year"
        })

        val mapsView = (google_map_view as SupportMapFragment)
        mapsView.getMapAsync { gmap ->
            val indonesiaLatLng = LatLng(-6.200000, 106.816666)
            gmap.moveCamera(CameraUpdateFactory.newLatLng(indonesiaLatLng))

            bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            bottom_sheet.container_main_info.animTo("Y", 25.dpf)
                            bottom_sheet.bottom_card.radius = 0.dpf
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            bottom_sheet.container_main_info.animTo("Y", 0.dpf)
                            bottom_sheet.bottom_card.radius = 26.dpf
                            gmap.setPadding(0, 0, 0, 130.dp)
                        }
                        else -> {
                            gmap.setPadding(0, 0, 0, 0)
                        }
                    }
                }
            })

            gmap.uiSettings.isMapToolbarEnabled = false
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

                viewModel.getTimeLine("").observe(this, Observer {
                    setupLineChart(it?.timeLine)
                })
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

    private fun setupDrawer() {
        val itemDownloadReport = primaryDrawer("WHO Documentation Report", R.drawable.ic_docs, 1L)
        val itemSources = primaryDrawer("Covid-19 Data Sources", R.drawable.ic_source, 2L)
        val itemFork = primaryDrawer("Fork Repository", R.drawable.ic_fork, 3L)
        val itemDoc = primaryDrawer("API Documentation", R.drawable.ic_api_docs, 4L)

        button_menu.setMarginTop(37.dp)

        val headerView = LayoutInflater.from(this).inflate(R.layout.header_drawer, null)

        val urlImage = "https://source.unsplash.com/featured/?covid,corona"
        val urlSources = "https://github.com/CSSEGISandData/COVID-19/blob/master/README.md"
        val urlRepo = "https://github.com/utsmannn/covid19-global-report"
        val urlDoc = "https://github.com/utsmannn/covid19-global-report/blob/master/DOCUMENTATION.md"

        Glide.with(this).load(urlImage).diskCacheStrategy(DiskCacheStrategy.DATA).into(headerView.image_header)

        val drawer = DrawerBuilder()
            .withActivity(this)
            .withHeader(headerView)
            .withSliderBackgroundColorRes(R.color.colorPrimary)
            .withHeaderHeight(DimenHolder.fromDp(200))
            .withSelectedItem(20L)
            .addDrawerItems(itemDownloadReport, itemSources, DividerDrawerItem(), itemFork, itemDoc)
            .withOnDrawerItemClickListener { view, position, drawerItem ->
                when (drawerItem.identifier) {
                    1L -> {
                        getSitRep()
                        false
                    }
                    2L -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlSources)))
                        false
                    }
                    3L -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlRepo)))
                        false
                    }
                    else -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlDoc)))
                        false
                    }
                }
            }
            .build()

        button_menu.setOnClickListener {
            drawer.openDrawer()
        }
    }

    private fun getSitRep() {
        viewModel.getSitRep().observe(this, Observer {
            val urlDownload = it?.downloadUrl
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlDownload)))

        })
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

            composite.delay(800) {
                bottomSheetBehavior.collapse(composite)
                setupPieChart(it)
            }
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

    private fun setupLineChart(data: List<DataTimeLine>?) {
        val valueFormat = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().formatted()
            }
        }

        val rawDataConfirmed: MutableList<Entry> = mutableListOf()
        val rawDataDeath: MutableList<Entry> = mutableListOf()
        val rawDataRecovered: MutableList<Entry> = mutableListOf()

        data?.forEachIndexed { index, dataTimeLine ->
            logi("index -> $index +++ data -> ${dataTimeLine.total.confirmed}")
            val dataLineConfirmed = Entry(index.toFloat(), dataTimeLine.total.confirmed.toFloat(), dataTimeLine.date)
            val dataLineDeath = Entry(index.toFloat(), dataTimeLine.total.death.toFloat(), dataTimeLine.date)
            val dataLineRecovered = Entry(index.toFloat(), dataTimeLine.total.recovered.toFloat(), dataTimeLine.date)

            rawDataConfirmed.add(dataLineConfirmed)
            rawDataDeath.add(dataLineDeath)
            rawDataRecovered.add(dataLineRecovered)
        }

        val lineDataSetConfirmed = LineDataSet(rawDataConfirmed, "Confirmed").apply {
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            valueFormatter = valueFormat
            color = ContextCompat.getColor(this@MainActivity, R.color.colorConfirmed)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setCircleColors(ContextCompat.getColor(this@MainActivity, R.color.colorConfirmed))
            setDrawCircleHole(false)
        }

        val lineDataSetDeath = LineDataSet(rawDataDeath, "Death").apply {
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            valueFormatter = valueFormat
            color = ContextCompat.getColor(this@MainActivity, R.color.colorDeath)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setCircleColors(ContextCompat.getColor(this@MainActivity, R.color.colorDeath))
            setDrawCircleHole(false)
        }

        val lineDataSetRecovered = LineDataSet(rawDataRecovered, "Recovered").apply {
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            valueFormatter = valueFormat
            color = ContextCompat.getColor(this@MainActivity, R.color.colorRecovered)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setCircleColors(ContextCompat.getColor(this@MainActivity, R.color.colorRecovered))
            setDrawCircleHole(false)
        }

        val lineData = LineData(lineDataSetConfirmed, lineDataSetDeath, lineDataSetRecovered)
        lineData.setValueTextColor(Color.WHITE)

        val xAxis = line_chart.xAxis
        xAxis.position = XAxis.XAxisPosition.TOP
        val yAxis = line_chart.axisRight


        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String?{
                return data?.map { it.date }?.get(value.toInt())
            }
        }

        val yFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String?{
                return ""
            }
        }

        xAxis.valueFormatter = formatter
        yAxis.valueFormatter = yFormatter
        line_chart.legend.textColor = ContextCompat.getColor(this, R.color.colorSubtitle)
        line_chart.description.text = ""
        line_chart.description.textColor = ContextCompat.getColor(this, R.color.colorSubtitle)

        line_chart.data = lineData
        line_chart.invalidate()

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

    private fun primaryDrawer(title: String, icon: Int, identifier: Long): PrimaryDrawerItem {
        return PrimaryDrawerItem()
            .withName(title)
            .withIcon(icon)
            .withIdentifier(identifier)
            .withTextColorRes(R.color.colorSubtitle)
            .withSelectedTextColor(Color.WHITE)
            .withSelectedColorRes(R.color.colorDrawerSelected)
            .withIconColorRes(R.color.colorDrawerSelected)
            .withSelectable(false)
    }
}
