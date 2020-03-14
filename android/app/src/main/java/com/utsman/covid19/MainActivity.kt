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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import com.utsman.covid19.cluster.CovidCluster
import com.utsman.covid19.cluster.CustomClusterRender
import com.utsman.covid19.ext.makeStatusBarTransparent
import com.utsman.covid19.network.ItemCluster
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import kotlinx.android.synthetic.main.covid_info_window.view.*

class MainActivity : AppCompatActivity() {

    private var covidCluster: CovidCluster? = null

    private val infoWindowAdapter = object : GoogleMap.InfoWindowAdapter {

        override fun getInfoContents(marker: Marker): View? {
            return null
        }

        override fun getInfoWindow(marker: Marker?): View? {
            val view = layoutInflater.inflate(R.layout.covid_info_window, null)
            view.info_title.text = covidCluster?.cluster?.data?.country
            return view
        }

    }

    private val viewModel: CovidViewModel by viewModels()
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(bottom_sheet)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeStatusBarTransparent()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_container)) { _, insets ->
            insets.consumeSystemWindowInsets()
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        viewModel.getData(12, 3)

        viewModel.total.observe(this, Observer {
            bottom_sheet.text_total.text = it.confirmed.toString()
            bottom_sheet.text_death.text = it.death.toString()
            bottom_sheet.text_recovered.text = it.recovered.toString()
        })

        val mapsView = (google_map_view as SupportMapFragment)
        mapsView.getMapAsync { gmap ->

            gmap.setOnMapClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
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
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
}
