package com.me94me.example_retrofit

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.me94me.example_retrofit.bean.JokeBean
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 项目仅学习使用
 */
class MainActivity : AppCompatActivity() {
    var tag = this.javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val interceptor = HttpLoggingInterceptor{
            Log.e(tag,it)
        }
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
        val retrofit = Retrofit.Builder()
                .client(client)

                .baseUrl("http://www.baidu.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val service = retrofit.create(Service::class.java)

        val call = service.getJoke()

        call.enqueue(object: Callback<List<JokeBean>?> {
            override fun onFailure(call: Call<List<JokeBean>?>?, t: Throwable?) {
                Log.e(tag,"onFailure")
            }

            override fun onResponse(call: Call<List<JokeBean>?>?, response: Response<List<JokeBean>?>?) {
                var body = response!!.body()
                Log.e(tag,"onResponse")
            }
        })
    }
    interface Service{

        @POST("/qq")
        fun getJoke():Call<List<JokeBean>>
    }
}
