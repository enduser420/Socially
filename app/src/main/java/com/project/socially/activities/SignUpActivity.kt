package com.project.socially.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project.socially.R
import com.project.socially.daos.UserDao
import com.project.socially.databinding.ActivitySignUpBinding
import com.project.socially.models.User

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.SignUp.setOnClickListener {
            signUpUser()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        finish()
    }

    private fun signUpUser() {
        if (binding.SignUpNametxt.text.toString().isEmpty()) {
            binding.SignUpNametxt.error = "Enter name"
            binding.SignUpNametxt.requestFocus()
            return
        }

        if (binding.SignUpEmailtxt.toString().isEmpty()) {
            binding.SignUpEmailtxt.error = "Enter Email!"
            binding.SignUpEmailtxt.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.SignUpEmailtxt.text.toString()).matches()) {
            binding.SignUpEmailtxt.error = "Enter valid Email"
            binding.SignUpEmailtxt.requestFocus()
            return
        }

        if (binding.SignUpPasswordtxt.text.toString().isEmpty()) {
            binding.SignUpPasswordtxt.error = "Enter Password"
            binding.SignUpPasswordtxt.requestFocus()
            return
        }

        if (binding.SignUpPasswordtxt.text.toString().isEmpty()) {
            binding.SignUpPasswordtxt.error = "Enter Password again"
            binding.SignUpPasswordtxt.requestFocus()
            return
        }

        if (binding.SignUpPasswordtxt.text.toString()
                .trim() == binding.ConfirmSignUpPasswordtxt.text.toString().trim()
        ) {
            auth.createUserWithEmailAndPassword(
                binding.SignUpEmailtxt.text.toString().trim(),
                binding.SignUpPasswordtxt.text.toString().trim()
            )
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful) {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthUserCollisionException) {
                            Toast.makeText(
                                baseContext, "Email already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: FirebaseAuthWeakPasswordException) {
                            Toast.makeText(
                                baseContext, "Password too weak",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                baseContext, "Something went wrong. Try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (task.isSuccessful) {
                        Log.d("SignIn", auth.currentUser!!.uid)
                        val userDao = UserDao()
                        val user =
                            User(
                                auth.currentUser!!.uid,
                                binding.SignUpNametxt.text.toString(),
                                binding.SignUpEmailtxt.text.toString().trim()
                            )
                        userDao.addUser(user)
                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                        finish()
                    } else {
                        Toast.makeText(
                            baseContext, "Something went wrong. Try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        } else {
            Toast.makeText(baseContext, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            binding.ConfirmSignUpPasswordtxt.error = "Enter Password again"
            binding.ConfirmSignUpPasswordtxt.requestFocus()
        }
    }

}