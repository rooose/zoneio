package com.example.zoneioclient

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)

        registerBtn.setOnClickListener { goToRegisterActivity() }
        loginButton.setOnClickListener { login() }
    }

    private fun login() {
        val username = username_input.text.toString()
        val password = password_input.text.toString()

        val parameters = mapOf("username" to username, "password" to password)
        val request = mapOf("endpoint" to "/login", "parameters" to parameters)
        val response = RequestHandler().execute(request)
        goToMainActivity(username)
    }

    private fun goToMainActivity(username: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("username", username)
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
}