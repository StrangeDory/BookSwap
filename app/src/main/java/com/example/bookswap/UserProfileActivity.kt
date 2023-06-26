package com.example.bookswap

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class UserProfileActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth
    private val databaseReference = Firebase.database.reference

    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        databaseReference.child("users").child(auth.currentUser!!.uid).child("username").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.username).text = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read value.", error.toException())
            }

        })
        databaseReference.child("users").child(auth.currentUser!!.uid).child("email").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.user_email).text = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read value.", error.toException())
            }

        })
        databaseReference.child("users").child(auth.currentUser!!.uid).child("fullname").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.user_Fullname).text = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read value.", error.toException())
            }

        })

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.popup).setOnClickListener{
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.popup_edit -> {
                        val intent = Intent(this, EditProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }
                    R.id.popup_logout -> {
                        auth.signOut()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.menu_profile)

            try{
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception) {
                Log.e("error", "Error showing menu icons!", e)
            } finally {
                popupMenu.show()
            }
        }
    }
}