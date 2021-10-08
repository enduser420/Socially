package com.project.socially.activities

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project.socially.R
import com.project.socially.daos.UserDao
import com.project.socially.databinding.ActivityLoginBinding
import com.project.socially.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN: Int = 420
    private var pressedTime: Long = 0
    private lateinit var backToast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        auth = Firebase.auth

        binding.LoginBtn.setOnClickListener {
            signIn()
        }

        binding.GoogleSignInBtn.setOnClickListener {
            googleSignIn()
        }

        binding.SignUpBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        binding.LoginEmailtxt.visibility = View.GONE
        binding.LoginPasswordtxt.visibility = View.GONE
        binding.LoginBtn.visibility = View.GONE
        binding.GoogleSignInBtn.visibility = View.GONE
        binding.SignUpBtn.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Dispatchers.Main) {
                updateUI(firebaseUser)
            }
        }
    }

    @SuppressLint("ShowToast")
    override fun onBackPressed() {
        backToast = Toast.makeText(
            baseContext, "Press back again to exit",
            Toast.LENGTH_SHORT
        )
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel()
            super.onBackPressed()
            super.finish()
        } else {
            backToast.show()
        }
        pressedTime = System.currentTimeMillis()
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    private fun signIn() {
        if (binding.LoginEmailtxt.toString().isEmpty()) {
            binding.LoginEmailtxt.error = "Enter Email!"
            binding.LoginEmailtxt.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.LoginEmailtxt.text.toString()).matches()) {
            binding.LoginEmailtxt.error = "Enter valid Email"
            binding.LoginEmailtxt.requestFocus()
            return
        }

        if (binding.LoginPasswordtxt.text.toString().isEmpty()) {
            binding.LoginPasswordtxt.error = "Enter Password"
            binding.LoginPasswordtxt.requestFocus()
            return
        }
        auth.signInWithEmailAndPassword(
            binding.LoginEmailtxt.text.toString().trim(),
            binding.LoginPasswordtxt.text.toString().trim()
        )
            .addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    try {
                        task.exception!!
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            baseContext, "Invalid credentials",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    updateUI(null)
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    binding.LoginEmailtxt.text?.clear()
                    binding.LoginPasswordtxt.text?.clear()
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val userDao = UserDao()
            val user = User(
                currentUser.uid,
                currentUser.displayName.toString().trim(),
                currentUser.email.toString().trim(),
                currentUser.photoUrl.toString()
            )
            userDao.addUser(user)
            startActivity(Intent(this, MainActivity::class.java))
            binding.LoginEmailtxt.text?.clear()
            binding.LoginPasswordtxt.text?.clear()
        } else {
            binding.LoginEmailtxt.visibility = View.VISIBLE
            binding.LoginPasswordtxt.visibility = View.VISIBLE
            binding.LoginBtn.visibility = View.VISIBLE
            binding.GoogleSignInBtn.visibility = View.VISIBLE
            binding.SignUpBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}