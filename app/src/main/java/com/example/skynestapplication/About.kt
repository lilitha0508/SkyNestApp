package com.example.skynestapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.viewpager2.widget.ViewPager2
import me.relex.circleindicator.CircleIndicator3

@OptIn(ExperimentalFoundationApi::class)
class About : ComponentActivity() {

    private val titleList = mutableListOf<String>()
    private val descList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val pager = findViewById<ViewPager2>(R.id.viewPager)
        infoToList()
        pager.adapter = ItemPageAdapter(titleList, descList)
        pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val indicator = findViewById<CircleIndicator3>(R.id.indicator)
        indicator.setViewPager(pager)
    }

    private fun addToList(title: String, description: String) {
        titleList.add(title)
        descList.add(description)
    }

    private fun infoToList() {
        for (i in 1..5) {
            addToList("Title $i", "Description $i")
        }
    }
}