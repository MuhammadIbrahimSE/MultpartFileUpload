package it.ibrahim.testapp
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import net.gotev.uploadservice.BuildConfig
import net.gotev.uploadservice.UploadServiceConfig.httpStack
import net.gotev.uploadservice.UploadServiceConfig.initialize
import net.gotev.uploadservice.UploadServiceConfig.placeholdersProcessor
import net.gotev.uploadservice.UploadServiceConfig.retryPolicy
import net.gotev.uploadservice.data.RetryPolicyConfig
import net.gotev.uploadservice.observer.request.GlobalRequestObserver
import net.gotev.uploadservice.okhttp.OkHttpStack

import net.gotev.uploadservice.UploadServiceConfig.defaultUserAgent

import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor


import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class App : Application() {
    companion object {
        var CHANNEL = "UploadServiceDemoChannel"
    }
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            //enableStrictMode();
        }

        createNotificationChannel()

        // Set your application namespace to avoid conflicts with other apps
        // using this library

        // Set your application namespace to avoid conflicts with other apps
        // using this library
        initialize(this, App.CHANNEL, BuildConfig.DEBUG)

        // Set up the Http Stack to use. If you omit this or comment it, HurlStack will be
        // used by default

        // Set up the Http Stack to use. If you omit this or comment it, HurlStack will be
        // used by default
        httpStack = OkHttpStack(getOkHttpClient()!!)

        // setup backoff multiplier

        // setup backoff multiplier
        retryPolicy = RetryPolicyConfig(1, 10, 2, 3)

        // you can add also your own custom placeholders to be used in notification titles and
        // messages

        // you can add also your own custom placeholders to be used in notification titles and
        // messages
        placeholdersProcessor = CustomPlaceholdersProcessor()

        // Uncomment to experiment Single Notification Handler
        // UploadServiceConfig.setNotificationHandlerFactory(ExampleSingleNotificationHandler::new);


        // Uncomment to experiment Single Notification Handler
        // UploadServiceConfig.setNotificationHandlerFactory(ExampleSingleNotificationHandler::new);
        GlobalRequestObserver(this, GlobalRequestObserverDelegate())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(App.CHANNEL, "Upload Service Demo", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getOkHttpClient(): OkHttpClient? {
        return OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val request = chain.request().newBuilder()
                            .header("User-Agent", defaultUserAgent)
                            .build()
                    chain.proceed(request)
                }) // you can add your own request interceptors to add authorization headers.
                // do not modify the body or the http method here, as they are set and managed
                // internally by Upload Service, and tinkering with them will result in strange,
                // erroneous and unpredicted behaviors
                .addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val request: Request.Builder = chain.request().newBuilder()
                            .addHeader("myheader", "myvalue")
                            .addHeader("mysecondheader", "mysecondvalue")
                    chain.proceed(request.build())
                }) // if you use HttpLoggingInterceptor, be sure to put it always as the last interceptor
                // in the chain and to not use BODY level logging, otherwise you will get all your
                // file contents in the log. Logging body is suitable only for small requests.
                .addInterceptor(HttpLoggingInterceptor { message -> Log.d("OkHttp", message) }.setLevel(HttpLoggingInterceptor.Level.HEADERS))
                .cache(null)
                .build()
    }

}
