package com.example.bookswap

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.bookswap.utils.WaitingDialog
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
import id.zelory.compressor.Compressor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth
    private val databaseReference = Firebase.database.reference
    private val storageRef = Firebase.storage.reference
    private var imageUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        //load account info
        databaseReference.child("users").child(auth.currentUser!!.uid).child("username").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.username_edit).text = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read value.", error.toException())
            }

        })
        databaseReference.child("users").child(auth.currentUser!!.uid).child("fullname").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.fullname_edit).text = value
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
                .into(findViewById<ImageView>(R.id.account_image_edit))
        }.addOnFailureListener { exception ->
            Log.e("error", exception.message.toString())
        }
        databaseReference.child("users").child(auth.currentUser!!.uid).child("email").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                findViewById<TextView>(R.id.email_edit).text = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Failed to read value.", error.toException())
            }

        })

        //pop up menu (camera or gallery)
        findViewById<CardView>(R.id.cv_add_icon_edit).setOnClickListener{
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
        findViewById<CardView>(R.id.cardViewIcon_edit).setOnClickListener{
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

        // save data
        findViewById<Button>(R.id.btn_save).setOnClickListener {
            if(checkAllFields()) {
                val waitDialog = WaitingDialog(this)
                waitDialog.startLoading()
                val username = findViewById<EditText>(R.id.username_edit).text.toString()
                val fullname = findViewById<EditText>(R.id.fullname_edit).text.toString()
                val email = findViewById<EditText>(R.id.email_edit).text.toString()
                databaseReference.child("users").child(auth.currentUser!!.uid).child("username").setValue(username).addOnCompleteListener {
                    if(it.isSuccessful){

                    }else{
                        Log.e("error", "Failed to write username!")
                    }
                }
                databaseReference.child("users").child(auth.currentUser!!.uid).child("fullname").setValue(fullname).addOnCompleteListener {
                    if(it.isSuccessful){

                    }else{
                        Log.e("error", "Failed to write fullname!")
                    }
                }
                databaseReference.child("users").child(auth.currentUser!!.uid).child("email").setValue(email).addOnCompleteListener {
                    if(it.isSuccessful){

                    }else{
                        Log.e("error", "Failed to write email!")
                    }
                }

                if(imageUri != null) {
                    storageRef.child("users/" + auth.currentUser!!.uid).delete()
                    storageRef.child("users/" + auth.currentUser!!.uid).putFile(imageUri!!).addOnCompleteListener {
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
    }

    private fun checkAllFields(): Boolean {
        val username = findViewById<EditText>(R.id.username_edit)
        val fullname = findViewById<EditText>(R.id.fullname_edit)
        val email = findViewById<EditText>(R.id.email_edit)
        if (username.text.toString().isEmpty()) {
            username.error = "User name is required!"
            return false
        }
        if(username.text.toString().length < 4) {
            username.error = "Username should be at least 4 characters!"
            return false
        }
        if (email.text.toString().isEmpty()) {
            email.error = "Email is required!"
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            email.error = "Check email format!"
            return false
        }
        if(fullname.text.toString().length < 4) {
            fullname.error = "Full name should be at least 4 characters!"
            return false
        }
        return true
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
                    findViewById<ImageView>(R.id.account_image_edit).setImageBitmap(ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, imageUri!!)))
                else
                    findViewById<ImageView>(R.id.account_image_edit).setImageBitmap(MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri!!))
            }
        }
        if(requestCode == 4 && resultCode == Activity.RESULT_OK && data != null) {
            saveImageToGallery(data.extras?.get("data") as Bitmap)
            val images: Bitmap = data.extras?.get("data") as Bitmap
            findViewById<ImageView>(R.id.account_image_edit).setImageBitmap(images)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}