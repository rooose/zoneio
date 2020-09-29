package com.example.zoneioclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_login.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)

        registerBtn.setOnClickListener { goToRegisterActivity() }
        loginButton.setOnClickListener { login() }

        val username = intent.getStringExtra("username")
        if (!username.isNullOrEmpty()) { username_input.setText(username) }
    }

    private fun login() {
        val username = username_input.text.toString()
        val password = password_input.text.toString()

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
            .url("http://192.168.0.110:8000/api/login")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                backgroundThreadShortToast(applicationContext, "An error happened: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = JSONObject(response.body!!.string())
                if(response.isSuccessful) {
                    backgroundThreadShortToast(applicationContext, "Successfuly logged in!")
                    goToMainActivity(username, body["token"] as String)
                } else {
                    backgroundThreadShortToast(applicationContext, body["error"] as String)
                }
            }
        })
    }

    private fun goToMainActivity(username: String, token: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("username", username)
            putExtra("token", token)
        }
        startActivity(intent)
    }

    private fun goToRegisterActivity() {
        val username = username_input.text
        val intent = Intent(this, RegisterActivity::class.java).apply {
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