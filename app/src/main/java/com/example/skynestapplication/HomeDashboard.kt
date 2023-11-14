package com.example.skynestapplication

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skynestapplication.databinding.ActivityCaptureBirdBinding
import com.example.skynestapplication.databinding.ActivityHomeDashboardBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class HomeDashboard : AppCompatActivity() {

    private val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("images")
    private val reference: StorageReference = FirebaseStorage.getInstance().reference
    private var imageUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1

    private lateinit var binding: ActivityHomeDashboardBinding
    private lateinit var pictureBinding: ActivityCaptureBirdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigate()

        pictureBinding = ActivityCaptureBirdBinding.inflate(layoutInflater)

        // Will take the user to the Map screen after clicking
        binding.ivHotsopt.setOnClickListener {
            val intent = Intent(this, Hotspots::class.java)
            startActivity(intent)
        }

        binding.ivGallery.setOnClickListener {
            val intent = Intent(this, Gallery::class.java)
            startActivity(intent)
        }

        binding.ivPhoto.setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMPORARY_IMAGE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMPORARY_IMAGE_DESCRIPTION")

        imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                pictureBinding.ivCapImage.setImageURI(imageUri)

                if (imageUri != null) {
                    uploadToFirebase(imageUri!!)
                }

                Toast.makeText(this, "Picture Saved", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, CaptureBird::class.java)
                intent.putExtra("imageUri", imageUri.toString())
                startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(this, ex.message.toString(), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this@HomeDashboard, "Cancelled...!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadToFirebase(uri: Uri) {
        val fileRef: StorageReference =
            reference.child(System.currentTimeMillis().toString() + "." + getFileExtension(uri))

        fileRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val model = Model(uri.toString())
                val modelId: String? = db.push().key
                db.child(modelId!!).setValue(model)
                Toast.makeText(this@HomeDashboard, "Uploaded Successfully", Toast.LENGTH_SHORT)
                    .show()

            }
        }
    }

    private fun getFileExtension(mUri: Uri?): String? {
        val cr: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(mUri!!))
    }

    private fun navigate() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.i_home -> {
                    val intent = Intent(this, HomeDashboard::class.java)
                    startActivity(intent)
                }

                R.id.i_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                }
                R.id.i_favourite -> {

                }
                else -> {}
            }
            true
        }
    }
}
