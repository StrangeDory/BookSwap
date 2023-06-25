package com.example.bookswap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth
    private val databaseReference = Firebase.database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (auth.currentUser != null) {
            databaseReference.child("users").child(auth.currentUser!!.uid).child("username").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue<String>()
                    findViewById<TextView>(R.id.account_name).text = value
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", "Failed to read value.", error.toException())
                }

            })
        }

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