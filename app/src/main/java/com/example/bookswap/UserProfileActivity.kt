package com.example.bookswap

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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


class UserProfileActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth
    private val databaseReference = Firebase.database.reference
    private val storageRef = Firebase.storage.reference
    private lateinit var recycleView: RecyclerView
    private lateinit var layoutManager:LinearLayoutManager
    private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Book, BooksProfileViewHolder>

    @SuppressLint("DiscouragedPrivateApi", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        recycleView = findViewById(R.id.rv_books_in_profile)
        layoutManager = LinearLayoutManager(this)
        recycleView.layoutManager = layoutManager
        recycleView.setHasFixedSize(true)
        logRecycleView()

        databaseReference.child("books").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                findViewById<TextView>(R.id.tv_number_records).text = dataSnapshot.childrenCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("error", databaseError.message.toString())
            }
        })

        databaseReference.child("users").child(auth.currentUser!!.uid).child("username").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.username).text = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read value.", error.toException())
            }

        })
        databaseReference.child("users").child(auth.currentUser!!.uid).child("email").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.user_email).text = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read value.", error.toException())
            }

        })
        databaseReference.child("users").child(auth.currentUser!!.uid).child("fullname").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.user_Fullname).text = value
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

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.btn_add_record).setOnClickListener {
            val intent = Intent(this, AddBookActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.popup).setOnClickListener{
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.popup_edit -> {
                        val intent = Intent(this, EditProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }
                    R.id.popup_logout -> {
                        auth.signOut()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
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

        findViewById<ImageView>(R.id.chats).setOnClickListener {
            val intent = Intent(this, UserChatsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun logRecycleView() {
        val options: FirebaseRecyclerOptions<Book> = FirebaseRecyclerOptions.Builder<Book>()
            .setQuery(databaseReference.child("books").child(auth.currentUser!!.uid), SnapshotParser<Book> { snapshot ->
                Book(
                    snapshot.child("name").value.toString(),
                    snapshot.child("author").value.toString(),
                    snapshot.child("description").value.toString(),
                    snapshot.child("comment").value.toString(),
                    snapshot.key.toString()
                )

            })
            .build()
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Book, BooksProfileViewHolder> (options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BooksProfileViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycle_view_item_books_in_profile, parent, false)
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
                val imageRef = storageRef.child("books/" + auth.currentUser!!.uid + "/" + model.id)
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