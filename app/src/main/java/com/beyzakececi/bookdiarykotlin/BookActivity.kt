package com.beyzakececi.bookdiarykotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.beyzakececi.bookdiarykotlin.databinding.ActivityBookBinding

class BookActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBookBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    fun saveButtonClicked(view : View){
        println("saveButtonClicked")
    }
    fun selectImage(view : View){
        println("selectImage")
    }
}