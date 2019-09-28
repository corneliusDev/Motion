package com.example.motion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.motion.UI.fragments.MotionLanding
import com.ramotion.fluidslider.FluidSlider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addFragment()

    }


    fun addFragment(){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = MotionLanding()
        fragmentTransaction.add(R.id.motion_container, fragment)
        fragmentTransaction.commit()
    }


}
