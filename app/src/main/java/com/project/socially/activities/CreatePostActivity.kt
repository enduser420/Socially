package com.project.socially.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.socially.R
import com.project.socially.daos.PostDao
import com.project.socially.databinding.ActivityCreatePostBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding

    private lateinit var postDao: PostDao
    private val IMAGE_REQUEST_CODE: Int = 69
    private lateinit var filePath: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filePath = Uri.EMPTY
        postDao = PostDao()
        binding.postBtn.setOnClickListener {
            val input = binding.postInput.text.toString()
            if (input.isNotEmpty()) {
                if (filePath == Uri.EMPTY) {
                    postDao.addPost(input)
                } else {
                    uploadImage(input)
                }
                finish()
                overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }
        }
        binding.postImage.setOnClickListener {
            pickImageGallery()
        }
    }

    private fun uploadImage(input: String) {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)
        val storageReference =
            FirebaseStorage.getInstance().getReference("images/$fileName")
        val uploadTask = storageReference.putFile(filePath)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation storageReference.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result.toString()
                postDao.addPost(input, downloadUri)
            }
        }.addOnFailureListener {
            Log.d("Failed", "Failed")
        }
    }

    private fun pickImageGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data == null || data.data == null) {
                filePath = Uri.EMPTY
            }
            filePath = data?.data!!
            try {
                binding.postImage.setImageURI(filePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }
}