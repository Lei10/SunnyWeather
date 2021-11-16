package com.project.sunnyweather

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication : Application(){

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        const val TOKEN = "5YrerPvmlpNhYIFi" //彩云天气Api的令牌值
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}