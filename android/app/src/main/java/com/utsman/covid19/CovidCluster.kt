package com.utsman.covid19

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class CovidCluster(val cluster: ItemCluster) : ClusterItem {
    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng {
        return cluster.latLng
    }
}