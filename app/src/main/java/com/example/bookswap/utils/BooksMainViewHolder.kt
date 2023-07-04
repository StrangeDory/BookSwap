package com.example.bookswap.utils

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.R
import com.squareup.picasso.Picasso

class BooksMainViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val userName = itemView.findViewById<TextView>(R.id.user_fullname_main)
    val userIcon = itemView.findViewById<ImageView>(R.id.user_account_image)
    val bookName = itemView.findViewById<TextView>(R.id.tv_title_main)
    val bookAuthor = itemView.findViewById<TextView>(R.id.tv_author_main)
    val bookDescription = itemView.findViewById<TextView>(R.id.tv_description_main)
    val bookComment = itemView.findViewById<TextView>(R.id.tv_comment_main)
    val imgBook = itemView.findViewById<ImageView>(R.id.rv_main_img_book)

    fun setUserName(name: String) {
        userName.text = name
    }

    fun setImgUser(uri: Uri) {
        Picasso.get()
            .load(uri)
            .into(userIcon)
    }

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

    fun setImgBook(uri: Uri) {
        Picasso.get()
            .load(uri)
            .into(imgBook)
    }

    fun clickUserProfile(uidCurrent: String) {
        userName.setOnClickListener{

        }
        userIcon.setOnClickListener {

        }
    }
}