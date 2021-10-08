package com.project.socially.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.project.socially.R
import com.project.socially.daos.UserDao
import com.project.socially.databinding.ActivityEditProfileBinding
import com.project.socially.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var userDao: UserDao
    private lateinit var user: User
    private val IMAGE_REQUEST_CODE: Int = 69
    private lateinit var filePath: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        userDao = UserDao()
        filePath = Uri.EMPTY
        GlobalScope.launch(Dispatchers.Main) {
            user = userDao.getUserById(auth.currentUser!!.uid).await().toObject(User::class.java)!!
            Glide.with(binding.profileImage.context)
                .load(user.imageUrl)
                .circleCrop()
                .into(binding.profileImage)

            binding.profileEmail.text = user.email
            binding.profileName.setText(user.displayName)
            binding.profileGender.setText(user.gender)
            binding.profileDOB.setText(user.dob)
            binding.profileAbout.setText(user.about)
        }

        binding.confirmProfileEdit.setOnClickListener {
            val user = User(
                user.uid,
                binding.profileName.text.toString(),
                user.email,
                user.imageUrl,
                binding.profileGender.text.toString(),
                binding.profileDOB.text.toString(),
                binding.profileAbout.text.toString()
            )
            if (filePath == Uri.EMPTY) {
                userDao.updateUser(user)
            } else {
                val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
                val now = Date()
                val fileName = formatter.format(now)
                val storageReference =
                    FirebaseStorage.getInstance().getReference("profiles/$fileName")
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
                        userDao.updateUser(user.uid, downloadUri)
                    }
                }.addOnFailureListener {
                    Log.d("Failed", "Failed")
                }
            }
            finish()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        binding.editDetails.setOnClickListener {
            startActivity(Intent(this, EditDetailsActivity::class.java))
            finish()
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }

        binding.profileImage.setOnClickListener {
            pickImageGallery()
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
                return
            }
            filePath = data?.data!!
            try {
                Glide.with(binding.profileImage.context)
                    .load(filePath)
                    .circleCrop()
                    .into(binding.profileImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}