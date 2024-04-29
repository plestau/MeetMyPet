package com.example.trabajo_final

import FragmentInferior
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentInferior = FragmentInferior()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_inferior, fragmentInferior)
            commit()
        }
    }
}