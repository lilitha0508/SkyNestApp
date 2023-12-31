package com.example.skynestapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.skynestapplication.databinding.ActivityCaptureBirdBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CaptureBird : AppCompatActivity() {

    private val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("images")
    private val reference: StorageReference = FirebaseStorage.getInstance().reference
    private var imageUri: Uri? = null

    private lateinit var binding: ActivityCaptureBirdBinding
    private lateinit var ivCapImg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptureBirdBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ivCapImg = findViewById(R.id.ivCapImage)


        // Retrieve the latest added image from Firebase
        db.orderByKey().limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val imageUrl: String? =
                            childSnapshot.child("imageData").getValue(String::class.java)

                        // Check if imageData is not null
                        if (imageUrl != null) {
                            // Use an image loading library like Glide or Picasso to load the image
                            // Here, I'll show you how to use Glide
                            Glide.with(this@CaptureBird)
                                .load(imageUrl)
                                .into(ivCapImg)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

    fun btnCapture(view: View) {
        val captureName = binding.tvCapName.text.toString()
        val captureDate = binding.tvCapDate.text.toString()
        val captureAmount = binding.tvCapAmount.text.toString()

        // Validate captureName
        if (captureName.isEmpty()) {
            binding.tvCapName.error = "Name is required"
            return
        }

        // Validate captureDate
        if (captureDate.isEmpty()) {
            binding.tvCapDate.error = "Date is required"
            return
        }

        // Validate captureAmount
        if (captureAmount.isEmpty()) {
            binding.tvCapAmount.error = "Amount is required"
            return
        }

        // Proceed with capturing and saving information
        val capture = itemDB(captureName, captureDate, captureAmount)
        val dataRef = FirebaseDatabase.getInstance().reference
        val id = dataRef.push().key
        db.child(id.toString()).setValue(capture)
            .addOnSuccessListener {
                binding.tvCapName.text.clear()
                binding.tvCapDate.text.clear()
                binding.tvCapAmount.text.clear()
                Toast.makeText(this, "Information Captured", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Information Not Captured", Toast.LENGTH_SHORT).show()
            }
    }


    private fun decodeBase64(input: String): Bitmap {
        val decodedBytes = android.util.Base64.decode(input, android.util.Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
