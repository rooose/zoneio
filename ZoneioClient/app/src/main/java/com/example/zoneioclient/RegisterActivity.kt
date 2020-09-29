package com.example.zoneioclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class RegisterActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        registerButton.setOnClickListener { register() }

        val username = intent.getStringExtra("username")
        register_username_input.setText(username)
    }

    private fun register() {
        val username = register_username_input.text.toString()
        val password = register_password_input.text.toString()

        if(username.isEmpty()){
            Toast.makeText(applicationContext, "No username specified", Toast.LENGTH_SHORT).show()
            return;
        }

        if(password.isEmpty()){
            Toast.makeText(applicationContext, "No password specified", Toast.LENGTH_SHORT).show()
            return;
        }

        val formBody: RequestBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        val request: Request = Request.Builder()
            .url("http://192.168.0.110:8000/api/register")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                backgroundThreadShortToast(applicationContext, "An error happened: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
               if(response.isSuccessful) {
                   backgroundThreadShortToast(applicationContext, "Successfuly registered!")
                   goToLoginActivity(username)
               } else {
                   val body = JSONObject(response.body!!.string())
                   backgroundThreadShortToast(applicationContext, body["error"] as String)
               }
            }
        })
    }

    private fun goToLoginActivity(username: String) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra("username", username)
        }
        startActivity(intent)
    }

    fun backgroundThreadShortToast(context: Context?, msg: String?) {
        if (context != null && msg != null) {
            Handler(Looper.getMainLooper()).post(Runnable {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            })
        }
    }
}
