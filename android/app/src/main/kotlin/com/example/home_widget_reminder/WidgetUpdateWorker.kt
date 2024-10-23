package com.example.home_widget_reminder

import android.content.Context
import android.content.SharedPreferences
import androidx.glance.appwidget.updateAll
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class WidgetUpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        println(">>>>>>> [WidgetUpdateWorker] doWork()")
        // Gọi API và cập nhật dữ liệu
        val data = fetchDataFromApi()
        // Lưu dữ liệu vào HomeWidgetGlanceState
        saveDataToWidgetState(data)

        // Cập nhật widget sau khi lưu dữ liệu
        CoroutineScope(Dispatchers.Main).launch {
            println(">>>>>>> [WidgetUpdateWorker] CoroutineScope(Dispatchers.Main).launch")
            CounterGlanceWidget().updateAll(applicationContext)
            CounterGlanceWidgetLong().updateAll(applicationContext)
        }

        return Result.success()
    }

    private fun fetchDataFromApi(): String {
        println(">>>>>>> [WidgetUpdateWorker] fetchDataFromApi()")
        val client = OkHttpClient()
        val request = Request.Builder().url("https://randomuser.me/api/?gender=female").build()

        return try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.take(100) ?: "No data"
            } else {
                "Error: ${response.code}"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun saveDataToWidgetState(data: String) {

        // Lưu dữ liệu vào SharedPreferences
        println(">>>>>> [WidgetUpdateWorker] saveDataToWidgetState: $data")
        val sharedPreferences: SharedPreferences =
            applicationContext.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("data", data)
            apply()
        }
        println(">>>>>> [WidgetUpdateWorker] save success")
    }
}
