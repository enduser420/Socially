package com.project.socially.activities

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.WindowManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.project.socially.R
import com.project.socially.databinding.ActivityImageFullscreenBinding

@Suppress("DEPRECATION")
class ImageFullscreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageFullscreenBinding

    var downloadId: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityImageFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(binding.postImageFS.context)
            .load(intent.getStringExtra("imgUrl"))
            .placeholder(R.mipmap.ic_launcher)
            .into(binding.postImageFS)

        binding.downloadImage.setOnClickListener {
            val title = System.currentTimeMillis().toString() + ".jpg"
            val req = DownloadManager.Request(Uri.parse(intent.getStringExtra("imgUrl").toString()))
                .setTitle(title)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)


            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = dm.enqueue(req)
        }

        val br = object:BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val id = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    Toast.makeText(applicationContext, "Downloaded", Toast.LENGTH_SHORT).show()
                }
            }
        }
        registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.slide_in_top,
            R.anim.slide_out_top
        )
    }
}