package com.example.skynestapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import me.relex.circleindicator.CircleIndicator3

class Features : AppCompatActivity() {

    private lateinit var btnSkip: Button
    private lateinit var indicator: CircleIndicator3
    private lateinit var vpSlide: ViewPager2
    private lateinit var adapter: featureAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_features)

        btnSkip = findViewById(R.id.btnSkip)
        indicator = findViewById(R.id.indicator)
        vpSlide = findViewById(R.id.vpSlide)

        adapter = featureAdapter(supportFragmentManager, lifecycle)

        vpSlide.adapter = adapter
        indicator.setViewPager(vpSlide)

        btnSkip.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}
