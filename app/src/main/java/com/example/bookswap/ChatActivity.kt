package com.example.bookswap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.utils.*
import com.example.bookswap.utils.adapter.ChatAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var chatList = ArrayList<Chat>()
    var topic = ""
    var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        chatRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)


        val intent = intent
        val uid = intent.getStringExtra("uid")


        findViewById<ImageView>(R.id.imgBack).setOnClickListener {
            val intent = Intent(this, UsersProfilesActivity::class.java).apply {
                putExtra("uid", uid)
            }
            startActivity(intent)
            finish()
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("users").child(uid!!)




        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Firebase.database.reference.child("users").child(uid!!).child("fullname").addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val value = snapshot.getValue<String>()
                        findViewById<TextView>(R.id.tvUserName).text = value
                        if (value != null) {
                            userName = value
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("error", "Failed to read value.", error.toException())
                    }

                })
                val storageRef = Firebase.storage.getReferenceFromUrl("gs://bookswap-d5092.appspot.com")
                val imageRef = storageRef.child("users/$uid")
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri)
                        .into(findViewById<ImageView>(R.id.imgProfile))
                }.addOnFailureListener {
                    findViewById<CircleImageView>(R.id.imgProfile).setImageResource(R.drawable.profile_image)
                }
            }
        })

        findViewById<ImageButton>(R.id.btnSendMessage).setOnClickListener {
            val message: String = findViewById<EditText>(R.id.etMessage).text.toString()

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "Message is empty!", Toast.LENGTH_SHORT).show()
                findViewById<EditText>(R.id.etMessage).setText("")
            } else {
                sendMessage(firebaseUser!!.uid, uid, message)
                findViewById<EditText>(R.id.etMessage).setText("")
                topic = "/topics/$uid"
                PushNotification(
                    NotificationData(userName,message),
                    topic).also {
                    sendNotification(it)
                }

            }
        }

        readMessage(firebaseUser!!.uid, uid)
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference

        val hashMap: HashMap<String, String> = HashMap()
        hashMap["senderId"] = senderId
        hashMap["receiverId"] = receiverId
        hashMap["message"] = message

        reference.child("Chat").push().setValue(hashMap)

    }

    private fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if (chat!!.senderId == senderId && chat.receiverId == receiverId ||
                        chat.senderId == receiverId && chat.receiverId == senderId
                    ) {
                        chatList.add(chat)
                    }
                }

                val chatAdapter = ChatAdapter(this@ChatActivity, chatList)

                chatRecyclerView.adapter = chatAdapter
            }
        })
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("TAG", response.errorBody()!!.string())
            }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }
}