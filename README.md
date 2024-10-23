# home_widget_reminder

# Introduction

- [HomeWidget](https://docs.page/abausg/home_widget) là một thư viện FLutter cho phép tạo và quản lý các widget trên màn hình chính của thiết bị di động. Giúp người dùng tương tác với ứng dụng thông qua home widget

# Concept

- HomeWidget và ứng dụng giao tiếp với nhau thông qua cơ chế shared data.
- Mặc định sử dụng UserDefaults trên iOS và SharedPreferences trên Android để lưu trữ và lấy dữ liệu

# Features

- https://docs.page/abausg/home_widget

#### 1. Tạo Widget

Một số guide để tạo được 1 widget Android/IOS cơ bản

- https://codelabs.developers.google.com/flutter-home-screen-widgets#5

- https://proandroiddev.com/when-jetpacks-glance-met-his-fellow-worker-work-manager-18cf19eff983

- https://medium.com/@ABausG/interactive-homescreen-widgets-with-flutter-using-home-widget-83cb0706a417

Chú ý:

- Tạo file BackgroundIntent cần tích vào các target

- ReminderWidgetExtension.entitlements -> chia sẻ dữ liệu giữa ứng dụng chính (Runner) và phần mở rộng Extension

- Trên Android việc xử lý background cần sử dụng workmanager với minimum interval là 15'

#### 2. Update Widget 

- Để update widget thì cần 2 step: 1 là lưu lại thông tin widget, sau đó gọi function update để update widget

- Ngoài ra HomeWidget cũng cho phép gọi funtion renderFlutterWidget để render flutter widget lên home. Bản chất của việc này là vẽ ảnh và lưu lại ảnh vào app storage với 1 key nhất đinh, và được override lại nội dung chứ ko phải tạo mới -> đảm bảo ko bị OOM.

~~~dart
Future<void> _sendAndUpdate([int? value]) async {
  await HomeWidget.saveWidgetData(_countKey, value);
  await HomeWidget.renderFlutterWidget(
    DashWithSign(count: value ?? 0),
    key: 'dash_counter',
    logicalSize: const Size(100, 100),
  );
  await HomeWidget.updateWidget(
    iOSName: 'ReminderWidget',
    androidName: 'CounterGlanceWidgetReceiver',
  );

  if (Platform.isAndroid) {
    // Update Glance Provider
    await HomeWidget.updateWidget(androidName: 'CounterGlanceWidgetReceiver');
  }
}
~~~

#### 3. Setup Android

3.1 Glance

- Glance: là 1 thư viện Android cho phép phát triển Widget cho ứng dụng 

- GlanceAppWidget: Đại diện cho 1 widget, cung cấp các phương thức và thuộc tính để xây dựng 1 widget.

- GlanceAppWidgetReceiver: Quản lý vòng đời của widget, lắng nghe update, bind, unbind...

3.2 Steps

- Update AndroidManifest.xml

- Trong android/app/build.gradle:

-  Thêm dependencies appwidget `androidx.glance:glance-appwidget:1.0.0`

-  Thêm build feature

~~~java
android {
    
    /// ...

    buildFeatures {
        compose true
    }

    // https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.1"
    }
}
~~~

~~~java
dependencies {
    implementation 'androidx.glance:glance-appwidget:1.0.0'
    // Call API
    implementation 'com.squareup.okhttp3:okhttp:4.9.1'
    // Work manager
    implementation "androidx.work:work-runtime-ktx:2.9.1"
}
~~~

3.3 Xử lý update Home Widget khi app ở trạng thái background/terminated

- Mặc định để update widget khi ở background hay terminated cần sử dụng workmanager

- Khi tương tác trực tiếp với widget -> tạo OneTimeWorkRequestBuilder -> handle -> update

- Khi muốn cập nhật widget theo 1 khoảng thời gian nhất định: Khi widget được bind vào màn hình chính, HomeWidgetGlanceWidgetReceiver onEnabled được gọi 1 lần duy nhất trong cả vòng đời widget.
Đây là method cần khởi tạo worker để thực hiện update định kỳ:

~~~java
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
~~~

#### 4. Setup IOS

- Thêm Widget Extension 
- Thêm Group 

4.1 Provider

- Provider: Là một struct tuân theo TimelineProvider, dùng để cung cấp dữ liệu cho widget,


### Cập nhật nhiều layout cho app

1. Để thêm các layout cho Android app:
- Tạo widget, widget receiver, layout xml mới, xác định sẵn targetCell

2. Để thêm các layout cho IOS app:

- Tạo 1 struct widget mới
- Thêm widget đó vào WidgetBundle


