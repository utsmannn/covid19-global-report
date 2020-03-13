package com.utsman.covid19

import android.content.Context
import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CustomClusterRender(
    context: Context,
    private val clusterManager: ClusterManager<CovidCluster>,
    map: GoogleMap
) : DefaultClusterRenderer<CovidCluster>(context, map, clusterManager) {

    override fun getClusterText(bucket: Int): String {
        return ""
    }

    override fun getColor(clusterSize: Int): Int {
        return Color.parseColor("#3CFF0000")
    }
}