package com.utsman.covid19

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val viewModel: CovidViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        makeStatusBarTransparent()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_container)) { _, insets ->
            insets.consumeSystemWindowInsets()
        }
        viewModel.getData(12, 3)

        viewModel.total.observe(this, Observer {
            text_total.text = it.confirmed.toString()
            text_death.text = it.death.toString()
            text_recovered.text = it.recovered.toString()
        })

        val mapsView = (google_map_view as SupportMapFragment)
        mapsView.getMapAsync { gmap ->
            val clusterManager = ClusterManager<CovidCluster>(this, gmap)
            val clusterAlgorithm = NonHierarchicalDistanceBasedAlgorithm<CovidCluster>()
            clusterAlgorithm.maxDistanceBetweenClusteredItems = 130

            clusterManager.algorithm = clusterAlgorithm
            clusterManager.renderer = CustomClusterRender(this, clusterManager, gmap)
            gmap.setOnCameraIdleListener(clusterManager)
            gmap.setOnMarkerClickListener(clusterManager)

            viewModel.data.observe(this, Observer { data ->
                val coordinate = data.map { it.coordinate }
                val latLngList = coordinate.map { LatLng(it[0], it[1]) }

                val dataMarkers = data.map { ItemMarker(it.country, LatLng(it.coordinate[0], it.coordinate[1])) }



                /*val aaa = dataMarkers.groupBy { it.title }
                aaa.map {

                }*/

                /*latLngList.forEach { latLng ->
                    val markerOption = MarkerOptions()
                        .position(latLng)

                    gmap.addMarker(markerOption)
                }*/

                //val covidClusterItem = latLngList.map { CovidCluster(it) }
                /*clusterManager.addItems(covidClusterItem)
                clusterManager.cluster()*/
            })

        }

    }
}
