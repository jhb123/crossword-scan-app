package com.jhb.crosswordScan.network

data class CrosswordAppRequest(
    var status: String,
    var message: String,
    var data: CrosswordAppRequestData
)
