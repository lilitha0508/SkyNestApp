package com.example.skynestapplication

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.skynestapplication.databinding.ActivityHomeDashboardBinding

class HomeDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityHomeDashboardBinding
    //private lateinit var picture: ActivityCaptureBirdBinding

    private var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigate()


        //Will take user to the Map screen after clicking
        binding.ivHotsopt.setOnClickListener {
            val intent = Intent(this, Hotspots::class.java)
            startActivity(intent)
        }

        binding.ivPhoto.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
                requestCameraPermissions.launch(cameraPermissions)
            } else {
                val cameraPermissions =
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestCameraPermissions.launch(cameraPermissions)
            }
        }
    }

    private val requestCameraPermissions =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback<Map<String, Boolean>> { result ->
                Log.d(TAG, "onActivityResult: ")
                Log.d(TAG, "onActivityResult: $result")

                //let's check if permissions are granted or not
                var areAllGranted = true
                for (isGranted in result.values) {
                    areAllGranted = areAllGranted && isGranted
                }
                if (areAllGranted) {
                    //All Permissions Camera, Storage are granted, we can now launch camera to capture image
                    pickImageCamera()
                } else {
                    Toast.makeText(
                        this@HomeDashboard,
                        "Camera or Storage or both permissions denied...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ")
        //Setup Content values, MediaStore to capture high quality image using camera intent
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMPORARY_IMAGE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMPORARY_IMAGE_DESCRIPTION")
        //Uri of the image to be captured from camera
        image_uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        //Intent to launch camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "onActivityResult: ")
        //Check if image is picked or not
        if (result.resultCode == RESULT_OK) {
            //no need to get image uri here we will have it in pickImageCamera() function
            Log.d(TAG, "onActivityResult: imageUri: $image_uri")
        } else {
            //Cancelled
            Toast.makeText(this@HomeDashboard, "Cancelled...!", Toast.LENGTH_SHORT).show()
        }
    }


    //--------------------------------------------------------------------------------------------------
    //Reference: Mafia Codes
    //URL: https://www.youtube.com/watch?v=oeKtwd1DBfg
    //Use: The bottom navigation bar will allow users to navigate through app
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
                    val intent = Intent(this, HomeDashboard::class.java)
                    startActivity(intent)


                }
                else -> {}
            }
            true
        }

    }

}