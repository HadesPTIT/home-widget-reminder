package com.example.home_widget_reminder

import HomeWidgetGlanceWidgetReceiver
import android.content.Context
import android.content.Intent
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class CounterGlanceWidgetReceiverLong : HomeWidgetGlanceWidgetReceiver<CounterGlanceWidgetLong>() {
    override val glanceAppWidget = CounterGlanceWidgetLong()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        println(">>>>> onReceive: ${intent.action}")
    }

    ///
    /// Chỉ được gọi 1 lần duy nhất trong suốt vòng đời của widget
    ///
    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        println(">>>>> onEnabled")
        if (context != null) {
            println(">>>>> scheduleWorker")
            scheduleWorker(context)
        }
    }

    private fun scheduleWorker(context: Context) {
        val workRequest =
            PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        println(">>>>> onDisabled")
    }
}