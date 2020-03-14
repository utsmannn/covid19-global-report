package com.utsman.covid19.cluster

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.utsman.covid19.network.ItemCluster

data class CovidCluster(val cluster: ItemCluster) : ClusterItem {
    override fun getSnippet(): String {
        return cluster.data.country
    }

    override fun getTitle(): String {
        return cluster.data.country
    }

    override fun getPosition(): LatLng {
        return cluster.latLng
    }
}