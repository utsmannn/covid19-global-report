package com.utsman.covid19

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import com.utsman.covid19.cluster.CovidCluster
import com.utsman.covid19.cluster.CustomClusterRender
import com.utsman.covid19.ext.*
import com.utsman.covid19.network.ItemCluster
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
            view.info_confirmed.text = covidCluster?.cluster?.data?.confirmed?.toString()
            view.info_death.text = covidCluster?.cluster?.data?.death?.toString()
            view.info_recovered.text = covidCluster?.cluster?.data?.recovered?.toString()

            bottom_sheet.text_title_report.text = title
            bottom_sheet.text_total.text = covidCluster?.cluster?.data?.confirmed?.toString()
            bottom_sheet.text_death.text = covidCluster?.cluster?.data?.death?.toString()
            bottom_sheet.text_recovered.text = covidCluster?.cluster?.data?.recovered?.toString()

            bottomSheetBehavior.collapse(composite)
            return view
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeStatusBarTransparent()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_container)) { _, insets ->
            insets.consumeSystemWindowInsets()
        }

        bottomSheetBehavior.hide()
        getTotal()

        viewModel.getLastDate().observe(this, Observer {
            val day = it.lastDate?.day
            val month = it.lastDate?.month
            viewModel.getData(day, month)
        })

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottom_sheet.container_main_info.animTo("Y", 25.dpf)

                } else {
                    bottomSheet.container_main_info.animTo("Y", 0.dpf)
                }
            }

        })

        val mapsView = (google_map_view as SupportMapFragment)
        mapsView.getMapAsync { gmap ->

            gmap.setOnMapClickListener {
                viewModel.total.value?.let {
                    bottom_sheet.text_title_report.text = "Worldwide"
                    bottom_sheet.text_total.text = it.confirmed.formatted()
                    bottom_sheet.text_death.text = it.death.formatted()
                    bottom_sheet.text_recovered.text = it.recovered.formatted()

                    bottomSheetBehavior.collapse(composite)
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

    private fun getTotal() {
        viewModel.total.observe(this, Observer {
            bottom_sheet.text_title_report.text = "Worldwide"
            bottom_sheet.text_total.text = it.confirmed.formatted()
            bottom_sheet.text_death.text = it.death.formatted()
            bottom_sheet.text_recovered.text = it.recovered.formatted()

            bottomSheetBehavior.collapse(composite)
        })

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
