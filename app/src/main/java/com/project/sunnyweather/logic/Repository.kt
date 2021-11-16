package com.project.sunnyweather.logic

import android.content.Context
import androidx.lifecycle.liveData
import com.project.sunnyweather.logic.model.DailyResponse
import com.project.sunnyweather.logic.model.Place
import com.project.sunnyweather.logic.model.Weather
import com.project.sunnyweather.logic.network.SunnyWeatherNetWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

object Repository {
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {//使所有代码运行在子线程中
        val placeResponse = SunnyWeatherNetWork.searchPlaces(query)
        if (placeResponse.status == "ok"){
            val places = placeResponse.places
            Result.success(places)
        }else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
            coroutineScope {//协程作用域
                val deferredRealtime = async {//保证两个网络请求都成功后，才会进一步执行程序
                    SunnyWeatherNetWork.getRealtimeWeather(lng, lat)
                }
                val deferredDaily = async {
                    SunnyWeatherNetWork.getDailyWeather(lng, lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse  = deferredDaily.await()
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok"){
                    val weather = Weather(realtimeResponse.result.realtime,dailyResponse.result.daily)
                    Result.success(weather)
                }else {
                    Result.failure(
                        RuntimeException(
                            "realtime response status is ${realtimeResponse.status}" + "daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
    }

    private fun <T> fire(context:CoroutineContext,block: suspend() -> Result<T>) = liveData<Result<T>>(context){
        val result = try {
            block()
        }catch (e: Exception){
            Result.failure<T>(e)
        }
        emit(result)
    }
}