package com.zia.widget.net

import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Created by AceMurder on 2018/1/24.
 */
object ApiGenerator {
    private const val DEFAULT_TIME_OUT = 30

    private val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
            .build()

    private val redrockRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://wx.idsbllp.cn/redapi2/api/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val ziaRetrofit = Retrofit.Builder()
            .baseUrl("http://zzzia.net:8080/version/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()


    fun <T> getRedrockService(clazz: Class<T>) = redrockRetrofit.create(clazz)

    fun <T> getZiaService(clazz: Class<T>) = ziaRetrofit.create(clazz)
}