package com.project.socially.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.project.socially.databinding.ActivityMainBinding
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.project.socially.adapters.IPostAdapter
import com.project.socially.adapters.PostAdapter
import com.project.socially.R
import com.project.socially.utilities.Utils
import com.project.socially.daos.PostDao
import com.project.socially.daos.UserDao
import com.project.socially.models.Post
import com.project.socially.models.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity(), IPostAdapter {

    private lateinit var binding: ActivityMainBinding

    private lateinit var postDao: PostDao
    private lateinit var adapter: PostAdapter
    private var pressedTime: Long = 0
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private lateinit var backToast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        actionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.edit_profile -> {
                    binding.drawerLayout.closeDrawers()
                    editProfile()
                }
                R.id.logout -> {
                    binding.drawerLayout.closeDrawers()
                    logOut()
                }
            }
            true
        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
        setUpNavHeader()
        setUpRecyclerView()
        timer()
    }
    
    private fun editProfile() {
        val i = Intent(this@MainActivity, EditProfileActivity::class.java)
        startActivity(i)
        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }

    private fun setUpNavHeader() {
        val navView: NavigationView = findViewById(R.id.navView)
        val headerView: View = navView.getHeaderView(0)
        val navImage: ImageView = headerView.findViewById(R.id.logged_user_image)
        val navUsername: TextView = headerView.findViewById(R.id.logged_username)
        val navUserEmail: TextView = headerView.findViewById(R.id.logged_user_email)
        val currentUserId = auth.currentUser!!.uid

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                val userDao = UserDao()
                val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
                Glide.with(this@MainActivity)
                    .load(user.imageUrl)
                    .error(R.mipmap.ic_launcher)
                    .placeholder(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(navImage)
                navUsername.text = user.displayName
                navUserEmail.text = user.email
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun logOut() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setMessage("Do you want to Logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                Firebase.auth.signOut()
                super.finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun setUpRecyclerView() {
        postDao = PostDao()
        val postsCollections = postDao.postCollections
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions =
            FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

//        adapter = PostAdapter(applicationContext, recyclerViewOptions, this)
        adapter = PostAdapter(recyclerViewOptions, this)

        binding.recyclerView.adapter = adapter
//        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

//        fixes the issue of loading main activity
        binding.recyclerView.itemAnimator = null

//        binding.recyclerView.scrollToPosition(0)
//        binding.recyclerView.scrollToPosition(adapter.itemCount);

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
            finishAffinity()
        } else {
            backToast.show()
        }
        pressedTime = System.currentTimeMillis()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun updateEverything() {
        adapter.notifyDataSetChanged()
    }

    private fun timer() {
        Handler(Looper.getMainLooper()).postDelayed({
            updateEverything()
        }, Utils.MINUTE_MILLIS)
    }

    /*override fun onPause() {
        super.onPause()
        adapter.startListening()
    }*/

    /*override fun onResume() {
        super.onResume()
        updateEverything()
    }*/

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }

    override fun ondisLikeClicked(postId: String) {
        postDao.updatedisLikes(postId)
    }
}

// Commenting
