package com.example.bookswap

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.utils.Book
import com.example.bookswap.utils.adapter.BooksMainAdapter
import com.example.bookswap.utils.adapter.BooksMainViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
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
    val bookList: MutableList<Book> = mutableListOf()
    var booksAdapter: BooksMainAdapter = BooksMainAdapter(bookList, this@MainActivity)

    @SuppressLint("MissingInflatedId")
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
            if (auth.currentUser != null) {
                val intent: Intent = Intent(this, UserProfileActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent: Intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
        }

        findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view_main).setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchList(newText)
                }
                return true
            }
        })
    }

    private fun logRecycleView() {
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
                booksAdapter = BooksMainAdapter(bookList, this@MainActivity)
                recycleView.adapter = booksAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read books nodes.", error.toException())
            }

        })
    }

    fun searchList(text: String) {
        val searchList = ArrayList<Book>()
        for (item in bookList) {
            if (item.name.lowercase().contains(text.lowercase()) || item.author.lowercase().contains(text.lowercase())) {
                searchList.add(item)
            }
        }
        booksAdapter.searchData(searchList)
    }

}
