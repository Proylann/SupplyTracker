package com.example.upang_supply_tracker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import kotlin.math.abs

class Home : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)



        val imageSlider = findViewById<ImageSlider>(R.id.imageSlider)

        val imageList = ArrayList<SlideModel>().apply {
            add(SlideModel(R.drawable.polo, ScaleTypes.CENTER_INSIDE))
            add(SlideModel(R.drawable.polo, ScaleTypes.CENTER_INSIDE))
            add(SlideModel(R.drawable.polo, ScaleTypes.CENTER_INSIDE))
        }

        imageSlider.setImageList(imageList)

    }
}

