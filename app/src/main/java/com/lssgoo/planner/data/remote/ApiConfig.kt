package com.lssgoo.planner.data.remote

object ApiConfig {
    private var baseUrl: String = "https://unfurnitured-stanford-soupy.ngrok-free.dev"

    fun getBaseUrl(): String = baseUrl

    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }
}
