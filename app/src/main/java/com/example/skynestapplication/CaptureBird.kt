package com.example.skynestapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
                        val imageData: String? =
                            childSnapshot.child("imageData").getValue(String::class.java)

                        // Check if imageData is not null
                        if (imageData != null) {
                            val bitmap: Bitmap = decodeBase64(imageData)

                            // Display the image in the ImageView
                            ivCapImg.setImageBitmap(bitmap)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }

    fun btnCapture(view: View) {
        val captureName = binding.tvCapName.text.toString()
        val captureDate = binding.tvCapDate.text.toString() // Corrected name
        val captureAmount = binding.tvCapAmount.text.toString() // Corrected name
        val capture = itemDB(captureName, captureDate, captureAmount)
        val dataRef = FirebaseDatabase.getInstance().reference
        val id = dataRef.push().key
        db.child(id.toString()).setValue(capture).addOnSuccessListener {
            binding.tvCapName.text.clear()
            binding.tvCapDate.text.clear() // Clear corrected name
            binding.tvCapAmount.text.clear() // Clear corrected name
            Toast.makeText(this, "Information Captured", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Information Not Captured", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeBase64(input: String): Bitmap {
        val decodedBytes = android.util.Base64.decode(input, android.util.Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
