package com.jhb.crosswordScan.data

import com.google.gson.annotations.SerializedName

data class ServerPuzzleListItem(
    @SerializedName("name") val name: String,
    @SerializedName("id") val id: Int,
    @SerializedName("file") val file: String
)
