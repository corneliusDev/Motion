package com.justin.motion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.justin.motion.UI.fragments.MotionLanding


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addFragment()

    }


    private fun addFragment(){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = MotionLanding()
        fragmentTransaction.add(R.id.motion_container, fragment)
        fragmentTransaction.commit()
    }


}
