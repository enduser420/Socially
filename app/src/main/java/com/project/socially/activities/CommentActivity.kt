package com.project.socially.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.project.socially.adapters.CommentAdapter
import com.project.socially.adapters.ICommentAdapter
import com.project.socially.R
import com.project.socially.utilities.Utils
import com.project.socially.daos.CommentDao
import com.project.socially.daos.PostDao
import com.project.socially.daos.UserDao
import com.project.socially.databinding.ActivityCommentBinding
import com.project.socially.models.Comment
import com.project.socially.models.User
import com.project.socially.utilities.COMMENT_REF
import com.project.socially.utilities.POST_REF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommentActivity : AppCompatActivity(), ICommentAdapter {

    private lateinit var binding: ActivityCommentBinding

    private lateinit var commentDao: CommentDao
    private lateinit var postDao: PostDao
    private lateinit var userDao: UserDao
    private lateinit var adapter: CommentAdapter
    private lateinit var user: User
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        val postId = intent.getStringExtra("postId").toString()
        GlobalScope.launch {
            userDao = UserDao()
            user = userDao.getUserById(auth.currentUser?.uid.toString()).await()
                .toObject(User::class.java)!!
        }
        postDao = PostDao()
        commentDao = CommentDao()
        binding.commentButton.setOnClickListener {
            if (binding.commentText.text.toString().isNotEmpty()) {
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        FirebaseFirestore.getInstance().runTransaction { transaction ->
                            val commentRef = FirebaseFirestore.getInstance().collection(POST_REF)
                                .document(postId).collection(COMMENT_REF).document()
                            val comment = Comment(
                                binding.commentText.text.toString(),
                                user,
                                System.currentTimeMillis(),
                            )
                            transaction.set(commentRef, comment)
                        }
                    }
                        .addOnSuccessListener {
                            binding.commentText.text?.clear()
                        }
                }
            } else {
                binding.commentText.error = "Enter Comment Text"
                binding.commentText.requestFocus()
            }
            timer()
        }

        val query = postDao.postCollections.document(postId).collection(COMMENT_REF)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions =
            FirestoreRecyclerOptions.Builder<Comment>().setQuery(query, Comment::class.java).build()

        adapter = CommentAdapter(recyclerViewOptions,postId, this)
        binding.commentRecyclerView.adapter = adapter
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentRecyclerView.itemAnimator = null
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun commentupdateEverything() {
        adapter.notifyDataSetChanged()
    }

    private fun timer() {
        Handler(Looper.getMainLooper()).postDelayed({
            commentupdateEverything()
        }, Utils.MINUTE_MILLIS)
    }

    override fun oncommentLikeClicked(postId: String, commentId: String) {
        commentDao.commentupdateLikes(postId, commentId)
    }

    override fun oncommentdisLikeClicked(postId: String, commentId: String) {
        commentDao.commentupdatedisLikes(postId, commentId)
    }

}