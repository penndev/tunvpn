package com.github.penndev

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.penndev.databinding.ActivityLoadingBinding

class LoadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoadingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onOpenHomePage(view: View) {
        val viewText = view as TextView
        val path = viewText.text.toString()
        intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
        startActivity(intent)
    }

    fun onStartMain(view:View) {
        startMainActivity()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}