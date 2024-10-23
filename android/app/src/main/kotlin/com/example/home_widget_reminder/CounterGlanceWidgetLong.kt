package com.example.home_widget_reminder

import HomeWidgetGlanceState
import HomeWidgetGlanceStateDefinition
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import es.antonborri.home_widget.actionStartActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CounterGlanceWidgetLong : GlanceAppWidget() {

    // Định nghĩa trạng thái cho lớp widget
    override val stateDefinition = HomeWidgetGlanceStateDefinition()

    // Định nghĩa nội dung sẽ được hiển thị
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Đặt lịch cho Worker chạy mỗi 1 phút
        println(">>>>>> provideGlance() called")
        provideContent {
            GlanceContent(context, currentState()) // Chỉ cần truyền context
        }
    }

    ///
    /// [GlanceContent] được recompose trong các trường hợp nào
    /// - Cập nhật widget
    /// - User tương tác với widget
    /// - Work manager cập nhật định kỳ
    /// - LaunchedEffect
    ///
    @Composable
    private fun GlanceContent(context: Context, currentState: HomeWidgetGlanceState) {


        println(">>>>>>> GlanceContent() called")

        // Lấy giá trị counter từ state
        val counter = currentState.preferences.getInt("counter", 0) // Lấy giá trị counter

        // Khai báo biến để lưu trữ dữ liệu từ API
        val data = remember { mutableStateOf<String?>(null) }

        // Lấy dữ liệu từ SharedPreferences
        val sharedPreferences = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
        data.value = sharedPreferences.getString("data", "Loading...")

        println(">>>>>> COUNTER: $counter DATA: ${data.value}")

        // Sử dụng LaunchedEffect để gọi API
        // Chỉ gọi lại khi tham số truyền vào thay đổi
        LaunchedEffect(counter) {
            println(">>>>>>> LaunchedEffect fetchDataFromApi()")
            data.value = fetchDataFromApi()
        }

        Box(
            modifier = GlanceModifier.background(Color.White).padding(16.dp)
                .clickable(onClick = actionStartActivity<MainActivity>(context))
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            ) {
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    data.value ?: "Loading...", // Hiển thị dữ liệu từ API hoặc thông báo đang tải
                    style = TextStyle(
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    ),
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    "Counter: $counter", // Hiển thị giá trị counter
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color.Red),
                        textAlign = TextAlign.Center
                    ),
                )
                Spacer(GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier.padding(10.dp).clickable(
                        onClick = actionRunCallback<RefreshActionLong>()
                    )
                ) {
                    Text(
                        "Refresh Data", style = TextStyle(
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                        )
                    )
                }
                Spacer(GlanceModifier.defaultWeight())
            }
        }
    }
}

class RefreshActionLong : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        println(">>>>>> RefreshAction")
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
    }
}

private suspend fun fetchDataFromApi(): String {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://randomuser.me/api/?gender=female") // Thay thế bằng URL API của bạn
            .build()

        try {
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
}