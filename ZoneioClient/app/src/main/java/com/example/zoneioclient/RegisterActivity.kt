package com.example.zoneioclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_login.*

class RegisterActivity : AppCompatActivity() {

    private val requestHandler: RequestHandler = RequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerButton.setOnClickListener { register() }
    }

    private fun register() {
        val username = register_username_input.text.toString()
        val password = register_password_input.text.toString()

        val parameters = mapOf("username" to username, "password" to password)
        val request = mapOf("endpoint" to "/register", "parameters" to parameters)
        val response = requestHandler.execute(request)
        goToMainActivity(username)

    }

    private fun goToMainActivity(username: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("username", username)
        }
        startActivity(intent)
    }

}
