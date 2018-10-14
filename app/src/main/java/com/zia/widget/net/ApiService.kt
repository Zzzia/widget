package com.zia.widget.net

import com.zia.widget.bean.Config
import com.zia.widget.bean.Course
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by zia on 2018/10/13.
 */
interface ApiService {
    @FormUrlEncoded
    @POST("get")
    fun getVersionConfig(@Field("key") key: String = "widget"): Call<Config>

    @FormUrlEncoded
    @POST("kebiao")
    fun getCourse(@Field("stuNum") stuNum: String): Call<String>
}