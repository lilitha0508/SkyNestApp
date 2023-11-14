package com.example.skynestapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Base64.decode
import androidx.appcompat.app.AppCompatActivity
import com.example.skynestapplication.databinding.ActivityGalleryBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Gallery : AppCompatActivity() {

    private lateinit var db: DatabaseReference
    private lateinit var binding: ActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseDatabase.getInstance().getReference("images")

        val gridView = binding.galBird

        // Retrieve images from Firebase
        db.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val imageList = mutableListOf<Bitmap>()

                for (childSnapshot in dataSnapshot.children) {
                    val imageData: String? =
                        childSnapshot.child("imageData").getValue(String::class.java)

                    // Check if imageData is not null
                    if (imageData != null) {
                        val bitmap: Bitmap = decodeBase64(imageData)
                        imageList.add(bitmap)
                    }
                }

                // Convert the list of bitmaps to an array of images
                val images = imageList.toTypedArray()

                val adapter = ImageAdapter(this@Gallery, images)
                gridView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    //---------------------------------------------------------------------------------------------------
    private fun decodeBase64(input: String): Bitmap {
        val decodedBytes = decode(input, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
    //---------------------------------------------------------------------------------------------------
}
