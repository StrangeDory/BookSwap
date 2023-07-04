package com.example.bookswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.utils.User
import com.example.bookswap.utils.adapter.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserChatsActivity : AppCompatActivity() {

    var userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_chats)

        findViewById<RecyclerView>(R.id.userRecyclerView).layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        findViewById<ImageView>(R.id.imgBack).setOnClickListener {
            onBackPressed()
        }
        getUsersList()
    }

    fun getUsersList() {
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        val userid = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")


        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("users")


        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val storageRef = Firebase.storage.getReferenceFromUrl("gs://bookswap-d5092.appspot.com")
                val imageRef = storageRef.child("users/" + firebase.uid)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri)
                        .into(findViewById<ImageView>(R.id.imgProfile))
                }.addOnFailureListener {
                    findViewById<CircleImageView>(R.id.imgProfile).setImageResource(R.drawable.icon_profile2)
                }

                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val user = User()
                    user.userId = dataSnapShot.key.toString()
                    user.userName = dataSnapShot.child("fullname").value.toString()

                    if (!user.userId.equals(firebase.uid)) {

                        userList.add(user)
                    }
                }

                val userAdapter = UserAdapter(this@UserChatsActivity, userList)

                findViewById<RecyclerView>(R.id.userRecyclerView).adapter = userAdapter
            }

        })
    }
}