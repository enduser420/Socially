package com.project.socially.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.socially.R
import com.project.socially.daos.PostDao
import com.project.socially.databinding.ActivityEditPostBinding

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostBinding

    private lateinit var postDao: PostDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val postId = intent.getStringExtra("postId").toString()
        binding.editPostInput.setText(intent.getStringExtra("postText").toString())
        postDao = PostDao()

        binding.editPostBtn.setOnClickListener {
            postDao.updatePost(postId, binding.editPostInput.text.toString())
            finish()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }
    }
}