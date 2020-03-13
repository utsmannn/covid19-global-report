package com.utsman.covid19

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class CovidCluster(val itemMarker: ItemMarker) : ClusterItem {
    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return itemMarker.title
    }

    override fun getPosition(): LatLng {
        return itemMarker.latLng
    }
}