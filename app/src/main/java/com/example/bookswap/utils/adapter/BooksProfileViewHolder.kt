package com.example.bookswap.utils.adapter

import android.app.Activity
import android.app.Dialog
import android.content.Intent.getIntent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class BooksProfileViewHolder(itemView: View, private val activity: Activity): RecyclerView.ViewHolder(itemView) {
    val bookName = itemView.findViewById<TextView>(R.id.rv_profile_book_name)
    val bookAuthor = itemView.findViewById<TextView>(R.id.rv_profile_book_author)
    val bookDescription = itemView.findViewById<TextView>(R.id.rv_profile_book_description)
    val bookComment = itemView.findViewById<TextView>(R.id.rv_profile_book_comment)
    val imgBook = itemView.findViewById<ImageView>(R.id.rv_profile_img_book)

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

    fun setClickListener(uid: String, idBook: String) {
        itemView.findViewById<ImageView>(R.id.delete_record).setOnClickListener{
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_delete)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val yesBtn = dialog.findViewById(R.id.btnYes) as Button
            val noBtn = dialog.findViewById(R.id.btnNo) as Button
            yesBtn.setOnClickListener {
                Firebase.database.reference.child("books").child(uid).child(idBook).removeValue()
                val storageRef = Firebase.storage.getReferenceFromUrl("gs://bookswap-d5092.appspot.com")
                val imageRef = storageRef.child("books/$uid").child(idBook)
                imageRef.delete().addOnSuccessListener {

                }.addOnFailureListener { exception ->
                    Log.e("error", exception.message.toString())
                }
                Firebase.database.reference.child("books").child(uid).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        activity.findViewById<TextView>(R.id.tv_number_records).text = dataSnapshot.childrenCount.toString()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("error", databaseError.message.toString())
                    }
                })
                dialog.dismiss()
            }
            noBtn.setOnClickListener {
                dialog.dismiss()

            }
            dialog.show()
        }
    }
}