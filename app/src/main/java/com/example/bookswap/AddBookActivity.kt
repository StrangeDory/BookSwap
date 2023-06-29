package com.example.bookswap

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.bookswap.utils.WaitingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddBookActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private var auth: FirebaseAuth = Firebase.auth
    private val databaseReference = Firebase.database.reference
    private val storageRef = Firebase.storage.reference
    private var countBooksPerUser: Long = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        findViewById<ImageButton>(R.id.backBtn_add_book).setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        databaseReference.child("books").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                countBooksPerUser = dataSnapshot.childrenCount
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("error", databaseError.message.toString())
            }
        })

        findViewById<Button>(R.id.btn_add).setOnClickListener {
            if(checkAllFields()){
                val waitDialog = WaitingDialog(this)
                waitDialog.startLoading()
                val bookName = findViewById<EditText>(R.id.book_name).text.toString()
                val bookAuthor = findViewById<EditText>(R.id.book_author).text.toString()
                val bookDescription = findViewById<EditText>(R.id.book_description).text.toString()
                val comment = findViewById<EditText>(R.id.comment).text.toString()
                val bookDB = databaseReference.child("books").child(auth.currentUser!!.uid)
                bookDB.child(countBooksPerUser.toString()).child("name").setValue(bookName).addOnCompleteListener {
                    if(it.isSuccessful){

                    }else{
                        Log.e("error", "Failed to write book name!")
                    }
                }
                bookDB.child(countBooksPerUser.toString()).child("author").setValue(bookAuthor).addOnCompleteListener {
                    if(it.isSuccessful){

                    }else{
                        Log.e("error", "Failed to write book author!")
                    }
                }
                bookDB.child(countBooksPerUser.toString()).child("description").setValue(bookDescription).addOnCompleteListener {
                    if(it.isSuccessful){

                    }else{
                        Log.e("error", "Failed to write book description!")
                    }
                }
                bookDB.child(countBooksPerUser.toString()).child("comment").setValue(comment).addOnCompleteListener {
                    if(it.isSuccessful){

                    }else{
                        Log.e("error", "Failed to write book comment!")
                    }
                }
                if(imageUri != null) {
                    storageRef.child("books/" + auth.currentUser!!.uid + "/" + countBooksPerUser).delete()
                    storageRef.child("books/" + auth.currentUser!!.uid + "/" + countBooksPerUser).putFile(imageUri!!).addOnCompleteListener {
                        if (it.isSuccessful) {

                        } else {
                            Log.e("error", "Failed to upload image!")
                        }
                    }
                }

                val intent = Intent(this, UserProfileActivity::class.java)
                val handler = Handler()
                handler.postDelayed(object: Runnable{
                    override fun run() {
                        waitDialog.hideLoading()
                        startActivity(intent)
                        finish()
                    }
                }, 3000)
            }
        }

        //pop up menu (camera or gallery)
        findViewById<ImageView>(R.id.img_book).setOnClickListener{
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.popup_gallery -> {
                        if(!checkGalleryPermission()){
                            requestGalleryPermission()
                        } else{
                            pickImageFromGallery()
                        }
                        true
                    }
                    R.id.popup_take_photo -> {
                        if(!checkCameraPermission()){
                            requestCameraPermission()
                        } else{
                            takePicture()
                        }
                        true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.menu_get_photo)

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
        findViewById<CardView>(R.id.cv_add_icon_book).setOnClickListener{
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.popup_gallery -> {
                        if(!checkGalleryPermission()){
                            requestGalleryPermission()
                        } else{
                            pickImageFromGallery()
                        }
                        true
                    }
                    R.id.popup_take_photo -> {
                        if(!checkCameraPermission()){
                            requestCameraPermission()
                        } else{
                            takePicture()
                        }
                        true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.menu_get_photo)

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
    }

    private fun checkCameraPermission(): Boolean {
        val result1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val result2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return result1 && result2
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestCameraPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 110)
    }

    private fun checkGalleryPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestGalleryPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 110 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePicture()
        }
        if(requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, 2)
    }

    private fun takePicture() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, 4)
            } else {
                Log.e("error", "No available camera!")
                Toast.makeText(this, "No available camera!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("error", "Camera not available!")
            Toast.makeText(this, "Camera not available!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAllFields(): Boolean {
        val bookName = findViewById<EditText>(R.id.book_name)
        val bookAuthor = findViewById<EditText>(R.id.book_author)
        if (bookName.text.toString().isEmpty()) {
            bookName.error = "Book name is required!"
            return false
        }
        if (bookAuthor.text.toString().isEmpty()) {
            bookAuthor.error = "Author is required!"
            return false
        }
        if(bookAuthor.text.toString().length < 3) {
            bookAuthor.error = "Author name should be at least 3 characters!"
            return false
        }
        return true
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val fileName =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".jpg"

        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }

        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            try {
                val outputStream = resolver.openOutputStream(it)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }


            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка сохранения изображения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            if(imageUri != null) {
                if(Build.VERSION.SDK_INT >= 28)
                    findViewById<ImageView>(R.id.img_book).setImageBitmap(
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(this.contentResolver, imageUri!!)))
                else
                    findViewById<ImageView>(R.id.img_book).setImageBitmap(MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri!!))
            }
        }
        if(requestCode == 4 && resultCode == Activity.RESULT_OK && data != null) {
            saveImageToGallery(data.extras?.get("data") as Bitmap)
            val images: Bitmap = data.extras?.get("data") as Bitmap
            findViewById<ImageView>(R.id.img_book).setImageBitmap(images)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}