package com.example.taller3.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taller3.R
import com.example.taller3.databinding.ActivitySeguimientoBinding

class SeguimientoActivity : AppCompatActivity() {
    lateinit var binding: ActivitySeguimientoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySeguimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}