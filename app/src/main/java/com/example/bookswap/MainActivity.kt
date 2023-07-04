package com.example.bookswap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.utils.Book
import com.example.bookswap.utils.BooksMainAdapter
import com.example.bookswap.utils.BooksMainViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
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

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth
    private val databaseReference = Firebase.database.reference
    private lateinit var recycleView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Book, BooksMainViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycleView = findViewById(R.id.recycler_view_books_main)
        layoutManager = LinearLayoutManager(this)
        recycleView.layoutManager = layoutManager
        recycleView.setHasFixedSize(true)
        logRecycleView()

        if (auth.currentUser != null) {
            databaseReference.child("users").child(auth.currentUser!!.uid).child("email").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue<String>()
                    if (value != null) {
                        findViewById<TextView>(R.id.account_name).text = if(value.length < 11) value else (value.substring(0, 9) + "...")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", "Failed to read value.", error.toException())
                }

            })
            val storageRef = Firebase.storage.getReferenceFromUrl("gs://bookswap-d5092.appspot.com")
            val imageRef = storageRef.child("users/" + auth.currentUser!!.uid)
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get()
                    .load(uri)
                    .into(findViewById<ImageView>(R.id.account_image))
            }.addOnFailureListener { exception ->
                Log.e("error", exception.message.toString())
            }
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

    private fun logRecycleView() {
        val bookList: MutableList<Book> = mutableListOf()
        databaseReference.child("books").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // Перебираем все дочерние узлы узла "books"
                for (childSnapshot in snapshot.children) {
                    // Получаем данные каждой книги из дочернего узла
                    for(childChildSnapshot in childSnapshot.children) {
                        val bookData = childChildSnapshot.value as HashMap<*, *>
                            val book = Book(
                                bookData["name"] as String,
                                bookData["author"] as String,
                                bookData["description"] as String,
                                bookData["comment"] as String,
                                childChildSnapshot.key as String,
                                childSnapshot.key.toString()
                            )
                            bookList.add(book)
                    }
                }
                val booksAdapter = BooksMainAdapter(bookList)
                recycleView.adapter = booksAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read books nodes.", error.toException())
            }

        })
    }

}