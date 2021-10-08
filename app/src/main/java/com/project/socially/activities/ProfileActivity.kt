package com.project.socially.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.project.socially.R
import com.project.socially.daos.UserDao
import com.project.socially.databinding.ActivityProfileBinding
import com.project.socially.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var userDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val i = intent
        userDao = UserDao()
        GlobalScope.launch(Dispatchers.Main) {
            val user = userDao.getUserById(i.getStringExtra("userId").toString()).await()
                .toObject(User::class.java)!!
            Glide.with(binding.profileImage.context)
                .load(user.imageUrl)
                .circleCrop()
                .into(binding.profileImage)
            binding.profileName.text = user.displayName
            binding.profileEmail.text = user.email
            binding.profileGender.text = user.gender
            binding.profileDOB.text = user.dob
            binding.profileAbout.text = user.about
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