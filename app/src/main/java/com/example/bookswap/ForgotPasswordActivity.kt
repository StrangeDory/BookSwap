package com.example.bookswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        findViewById<Button>(R.id.confirmBtn).setOnClickListener{
            val email = findViewById<EditText>(R.id.editEmailForgot).text.toString()
            if(checkEmail()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Toast.makeText(this, "Email sent!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkEmail(): Boolean {
        val email = findViewById<EditText>(R.id.editEmailForgot)
        if (email.text.toString().isEmpty()) {
            email.error = "Email is required!"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            email.error = "Check email format!"
            return false
        }
        return true
    }
}