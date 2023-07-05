package com.example.bookswap.utils.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.R
import com.example.bookswap.utils.Book
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class BooksMainAdapter(private var bookList: List<Book>, private val activity: Activity) : RecyclerView.Adapter<BooksMainViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksMainViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item_books_main, parent, false)
        return BooksMainViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: BooksMainViewHolder, position: Int) {
        val book = bookList[position]
        holder.clickUserProfile(book.uid)
        holder.setBookName(book.name)
        holder.setBookAuthor(book.author)
        holder.setBookDescription(book.description)
        holder.setComment(book.comment)
        Firebase.database.reference.child("users").child(book.uid).child("fullname").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.setUserName(snapshot.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read full name.", error.toException())
            }

        })
        val storageRef = Firebase.storage.getReferenceFromUrl("gs://bookswap-d5092.appspot.com")
        val iconUserRef = storageRef.child("users/" + book.uid)
        iconUserRef.downloadUrl.addOnSuccessListener { uri ->
            holder.setImgUser(uri)
        }.addOnFailureListener { exception ->
            Log.e("error", exception.message.toString())
        }
        val imageRef = storageRef.child("books/" + book.uid + "/" + book.id)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            holder.setImgBook(uri)
        }.addOnFailureListener { exception ->
            Log.e("error", exception.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchData(searchList: ArrayList<Book>) {
        bookList = searchList
        notifyDataSetChanged()
    }
}