package com.zia.widget.net

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by zia on 2018/10/16.
 */
object Service {
    val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
}