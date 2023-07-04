package com.example.bookswap

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.utils.Book
import com.example.bookswap.utils.adapter.BooksProfileViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class UsersProfilesActivity : AppCompatActivity() {

    var uid: String? = ""
    private val databaseReference = Firebase.database.reference
    private var auth: FirebaseAuth = Firebase.auth
    private lateinit var recycleView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Book, BooksProfileViewHolder>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_profiles)

        uid = intent.getStringExtra("uid")

        recycleView = findViewById(R.id.rv_books_in_profile)
        layoutManager = LinearLayoutManager(this)
        recycleView.layoutManager = layoutManager
        recycleView.setHasFixedSize(true)
        logRecycleView()

        databaseReference.child("books").child(uid!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                findViewById<TextView>(R.id.tv_number_records).text = dataSnapshot.childrenCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("error", databaseError.message.toString())
            }
        })

        databaseReference.child("users").child(uid!!).child("username").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.username).text = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read value.", error.toException())
            }

        })
        databaseReference.child("users").child(uid!!).child("email").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.user_email).text = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read value.", error.toException())
            }

        })
        databaseReference.child("users").child(uid!!).child("fullname").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.user_Fullname).text = value
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
                .into(findViewById<ImageView>(R.id.account_image))
        }.addOnFailureListener { exception ->
            Log.e("error", exception.message.toString())
        }

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.icon_chat).setOnClickListener{
            if(auth.currentUser != null) {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("uid", uid)
                    putExtra("isInProfile", "false")
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "You need to sign in to write messages!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logRecycleView() {
        val options: FirebaseRecyclerOptions<Book> = FirebaseRecyclerOptions.Builder<Book>()
            .setQuery(databaseReference.child("books").child(uid!!), SnapshotParser<Book> { snapshot ->
                Book(
                    snapshot.child("name").value.toString(),
                    snapshot.child("author").value.toString(),
                    snapshot.child("description").value.toString(),
                    snapshot.child("comment").value.toString(),
                    snapshot.key.toString()
                )

            })
            .build()
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Book, BooksProfileViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BooksProfileViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_item_books, parent, false)
                return BooksProfileViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: BooksProfileViewHolder,
                position: Int,
                model: Book
            ) {
                holder.setBookName(model.name)
                holder.setBookAuthor(model.author)
                holder.setBookDescription(model.description)
                holder.setComment(model.comment)
                val storageRef = Firebase.storage.getReferenceFromUrl("gs://bookswap-d5092.appspot.com")
                val imageRef = storageRef.child("books/" + uid + "/" + model.id)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    holder.setImgBook(uri)
                }.addOnFailureListener { exception ->
                    Log.e("error", exception.message.toString())
                }
            }

        }

        recycleView.adapter = firebaseRecyclerAdapter
    }

    override fun onStart() {
        super.onStart()
        firebaseRecyclerAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        firebaseRecyclerAdapter.startListening()
    }
}