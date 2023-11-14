package com.example.skynestapplication

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class ImageAdapter(private val context: Context, private val images: Array<Bitmap>) :
    BaseAdapter() {

    override fun getCount(): Int {
        return images.size
    }

    override fun getItem(position: Int): Any {
        return images[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView

        if (convertView == null) {
            // If convertView is null, inflate the layout
            imageView = ImageView(context)

            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = ViewGroup.LayoutParams(350, 350) // Adjust the size as needed
        } else {
            imageView = convertView as ImageView
        }

        // Set the image bitmap to the ImageView
        imageView.setImageBitmap(images[position])

        return imageView
    }
}
