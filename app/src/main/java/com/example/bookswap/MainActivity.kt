package com.example.bookswap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<LinearLayout>(R.id.account_image_layout).setOnClickListener {
            val intent: Intent = if (auth.currentUser != null) {
                Intent(this, UserProfileActivity::class.java)
            } else {
                Intent(this, SignInActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
}