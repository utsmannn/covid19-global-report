package com.utsman.covid19.api.raw_model


import com.fasterxml.jackson.annotation.JsonProperty

data class RawMasterRecursive(
    @JsonProperty("sha")
    var sha: String?,
    @JsonProperty("url")
    var url: String?,
    @JsonProperty("tree")
    var tree: List<Tree?>?,
    @JsonProperty("truncated")
    var truncated: Boolean?
)

data class Tree(
        @JsonProperty("path")
        var path: String?,
        @JsonProperty("mode")
        var mode: String?,
        @JsonProperty("type")
        var type: String?,
        @JsonProperty("sha")
        var sha: String?,
        @JsonProperty("size")
        var size: Int?,
        @JsonProperty("url")
        var url: String?
)