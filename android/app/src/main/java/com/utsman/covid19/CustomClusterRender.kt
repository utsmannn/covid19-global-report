package com.utsman.covid19

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.main.marker_view.view.*
import kotlinx.android.synthetic.main.marker_view.view.text_marker
import kotlinx.android.synthetic.main.marker_view_cluster.view.*

class CustomClusterRender(
    private val context: Context,
    clusterManager: ClusterManager<CovidCluster>,
    map: GoogleMap
) : DefaultClusterRenderer<CovidCluster>(context, map, clusterManager) {

    private val markerView = LayoutInflater.from(context).inflate(R.layout.marker_view, null)

    @SuppressLint("InflateParams")
    override fun onBeforeClusterItemRendered(item: CovidCluster, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        markerView.text_marker.text = item.cluster.title.toString()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(context, markerView)))
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<CovidCluster>?,
        markerOptions: MarkerOptions
    ) {
        val markerView = LayoutInflater.from(context).inflate(R.layout.marker_view_cluster, null)
        val size = cluster?.items?.sumBy { it.cluster.title }
        val sizeString = if (size.toString().length > 3) "${size.toString().subSequence(0,3)}++" else size.toString()
        markerView.text_marker.text = sizeString
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(context, markerView)))
    }
}