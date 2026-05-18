package com.example.financialtransactions

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_sign_up)

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            // Mock signup
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            finish() // Return to Login
        }

        findViewById<TextView>(R.id.tvBackToLogin).setOnClickListener {
            finish()
        }
    }
}
