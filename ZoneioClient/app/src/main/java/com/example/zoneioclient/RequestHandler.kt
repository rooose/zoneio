package com.example.zoneioclient

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import okhttp3.*
import okio.IOException
import org.json.JSONObject


class RequestHandler: AsyncTask<Map<String, Any>, Void, String>() {

    private val client: OkHttpClient = OkHttpClient()

    private fun sendData(endpoint: String, params: Map<String, Any>): String? {
        return try {
            val formBody: FormBody.Builder = FormBody.Builder();
            for((key, value) in params) {
                formBody.add(key, value as String)
            }
            val body: RequestBody = formBody.build();
            val request: Request = Request.Builder()
                .url("http://localhost:5000/$endpoint")
                .post(body)
                .build()
            val response: Response = client.newCall(request).execute()
            response.body?.string()
        } catch (e: IOException) {
            "Error: " + e.message
        }
    }

    protected override fun doInBackground(vararg params: Map<String, Any>): String? {
        val endpoint: String = params[0]["endpoint"] as String;
        val requestParams: Any = params[0]["params"] ?: error("Missing parameters");
        return sendData(endpoint, requestParams as Map<String, Any>)
    }

    protected override fun onPostExecute(response: String?) {
        Log.d("networking", response)
    }
}