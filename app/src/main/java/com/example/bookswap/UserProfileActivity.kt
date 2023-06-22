package com.example.bookswap

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserProfileActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth

    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        findViewById<ImageView>(R.id.popup).setOnClickListener{
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.popup_edit -> {
                        //val intent = Intent()
                        true
                    }
                    R.id.popup_logout -> {
                        auth.signOut()
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