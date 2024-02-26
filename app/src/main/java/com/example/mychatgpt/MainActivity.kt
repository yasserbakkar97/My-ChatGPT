package com.example.mychatgpt

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.e
import android.util.Log.v
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    // Constants
    private val apiKey = "your_apiKey"
    private val apiUrl = "https://api.openai.com/v1/completions"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        etQuestion.setOnClickListener {
            if (isNetworkAvailable()) {
                val question = etQuestion.text.toString()
                if (question.isNotEmpty()) {
                    txtResponse.text = "Please wait.."
                    getResponse(question) { response ->
                        runOnUiThread {
                            txtResponse.text = response
                        }
                    }
                }
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun getResponse(question: String, callback: (String) -> Unit) {
        etQuestion.setText("")
        val requestBody = """
            {
             "model": "gpt-3.5-turbo-instruct",
             "prompt": "$question",
             "max_tokens": 500,
             "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e("error", "API Failed", e)
                Toast.makeText(this@MainActivity, "Try Again..", Toast.LENGTH_SHORT).show()
                txtResponse.text = ""
            }


            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    v("Mydata", body)
                    val jsonObject = JSONObject(body)
                    val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                    val textResult = jsonArray.getJSONObject(0).getString("text")
                    callback(textResult)
                } else {
                    v("Mydata", "empty")
                }
            }
        })
    }
}