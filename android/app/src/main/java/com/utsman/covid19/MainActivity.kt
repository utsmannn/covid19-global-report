package com.utsman.covid19

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
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
        //viewModel.getDataCountry(12, 3)

        viewModel.total.observe(this, Observer {
            text_total.text = it.confirmed.toString()
            text_death.text = it.death.toString()
            text_recovered.text = it.recovered.toString()
        })

        val mapsView = (google_map_view as SupportMapFragment)
        mapsView.getMapAsync { gmap ->

            viewModel.data.observe(this, Observer { data ->

                val clusterManager = ClusterManager<CovidCluster>(this, gmap)
                clusterManager.renderer = CustomClusterRender(this, clusterManager, gmap)

                gmap.setOnCameraIdleListener(clusterManager)
                gmap.setOnMarkerClickListener(clusterManager)

                clusterManager.setOnClusterItemClickListener {
                    gmap.animateCamera(CameraUpdateFactory.newLatLng(it.position))
                    true
                }

                data.forEach {
                    val latLng = LatLng(it.coordinate[0], it.coordinate[1])
                    val itemCluster = ItemCluster(it.confirmed ?: 0, latLng)
                    clusterManager.addItem(CovidCluster(itemCluster))
                    clusterManager.cluster()
                }
            })

        }

    }
}
