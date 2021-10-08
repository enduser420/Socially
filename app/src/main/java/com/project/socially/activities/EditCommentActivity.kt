package com.project.socially.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.socially.R
import com.project.socially.daos.CommentDao
import com.project.socially.databinding.ActivityEditCommentBinding

class EditCommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCommentBinding

    private lateinit var commentDao: CommentDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postId = intent.getStringExtra("postId").toString()
        val commentId = intent.getStringExtra("commentId").toString()
        binding.editCommentInput.setText(intent.getStringExtra("oldComment").toString())
        commentDao = CommentDao()

        binding.editCommentBtn.setOnClickListener {
            commentDao.updateComment(postId, commentId, binding.editCommentInput.text.toString())
            finish()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }
    }
}