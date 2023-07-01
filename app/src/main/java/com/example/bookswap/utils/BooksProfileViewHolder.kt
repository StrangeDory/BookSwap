package com.example.bookswap.utils

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.R

class BooksProfileViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val bookName = itemView.findViewById<TextView>(R.id.rv_profile_book_name)
    val bookAuthor = itemView.findViewById<TextView>(R.id.rv_profile_book_author)
    val bookDescription = itemView.findViewById<TextView>(R.id.rv_profile_book_description)
    val bookComment = itemView.findViewById<TextView>(R.id.rv_profile_book_comment)

    fun setBookName(name: String) {
        bookName.text = name
    }

    fun setBookAuthor(author: String) {
        bookAuthor.text = author
    }

    fun setBookDescription(description: String) {
        bookDescription.text = description
    }

    fun setComment(comment: String) {
        bookComment.text = comment
    }
}