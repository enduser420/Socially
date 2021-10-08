package com.project.socially.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project.socially.R
import com.project.socially.daos.UserDao
import com.project.socially.databinding.ActivityDetailsProfileBinding
import com.project.socially.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class EditDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        binding.confirmProfileEdit.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Updating Profile")
            progressDialog.setMessage("Please wait ...")
            progressDialog.show()
            editProfile()
        }
        
        /*binding.confirmDeleteProfile.setOnClickListener {
            userDao = UserDao()
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to delete this User?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    userDao.deleteUser(auth.currentUser!!.uid)
                    Firebase.auth.signOut()
                    super.finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }*/

    }

    private fun editProfile() {
        val currentUser = auth.currentUser
        val credential = EmailAuthProvider.getCredential(
            currentUser!!.email.toString().trim(),
            binding.editProfilePassword.text.toString().trim()
        )

        currentUser.reauthenticate(credential).addOnCompleteListener {
            val userDao = UserDao()
            currentUser.updateEmail(binding.editProfileEmail.text.toString().trim())
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful) {
                        Log.d("Update Error", task.exception.toString())
                    } else {
                        GlobalScope.launch(Dispatchers.Main) {
                            val localuser = userDao.getUserById(currentUser.uid).await()
                                .toObject(User::class.java)!!
                            val user = User(
                                localuser.uid,
                                localuser.displayName,
                                binding.editProfileEmail.text.toString().trim(),
                                localuser.imageUrl,
                                localuser.gender,
                                localuser.dob,
                                localuser.about
                            )
                            userDao.updateUser(user)
                        }
                        finish()
                        overridePendingTransition(
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                    }
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