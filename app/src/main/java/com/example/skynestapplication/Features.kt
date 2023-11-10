package com.example.skynestapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Features : AppCompatActivity() {

    private lateinit var backgroundImageView: ImageView
    private lateinit var btnSkip: Button
    private lateinit var tvMessage: TextView
    private var imageIndex = 1
    private var stringIndex = 1
    private val imageArray = intArrayOf(R.drawable.birds1, R.drawable.birds2, R.drawable.birds3)
    private val stringArray = arrayOf(
        "Welcome to SkyNest",
        "Skynest is an Interactive app that helps bring bird loves together",
        "Skynest has featues like a field Guide, Bird Hotspts and the ability to capture these beatiful creaturs"
    )


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_features)

        backgroundImageView = findViewById(R.id.backgroundImageView)
        btnSkip = findViewById(R.id.btnSkip)
        tvMessage = findViewById(R.id.tvMessage)

        backgroundImageView.setOnClickListener {
            // Change the background image
            imageIndex = (imageIndex + 1) % imageArray.size
            backgroundImageView.setImageResource(imageArray[imageIndex])

            stringIndex =(stringIndex + 1) % stringArray.size
            tvMessage.text = stringArray[stringIndex]


            btnSkip.setOnClickListener {
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }
    }
}
