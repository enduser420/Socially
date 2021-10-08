package com.project.socially.daos


import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.project.socially.models.Post
import com.project.socially.models.User
import com.project.socially.utilities.POST_REF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {
    private val db = FirebaseFirestore.getInstance()
    val postCollections = db.collection(POST_REF)
    private val auth = Firebase.auth

    fun addPost(text: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime = System.currentTimeMillis()
            val post = Post(text, user, currentTime)
            postCollections.document().set(post)
        }
    }

    fun addPost(text: String, photoUrl: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime = System.currentTimeMillis()
            val post = Post(text, user, currentTime, photoUrl)
            postCollections.document().set(post)
        }
    }

    fun updatePost(postId: String, newtext: String) {
        GlobalScope.launch {
            val oldPost = getPostById(postId).await().toObject(Post::class.java)!!
            val user = oldPost.createdBy
            val liked = oldPost.likedBy
            val disliked = oldPost.dislikedBy
            val currentTime = oldPost.createdAt
            val uri = oldPost.photoUrl
            val post = Post(newtext, user, currentTime, uri, liked, disliked)
            postCollections.document(postId).set(post, SetOptions.merge())
        }
    }

    private fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollections.document(postId).get()
    }

    fun updateLikes(postId: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)!!
            val isLiked = post.likedBy.contains(currentUserId)
            if (isLiked) {
                post.likedBy.remove(currentUserId)
            } else {
                if (post.dislikedBy.contains(currentUserId)){
                    post.dislikedBy.remove(currentUserId)
                }
                post.likedBy.add(currentUserId)
            }
            postCollections.document(postId).set(post)
        }
    }

    fun updatedisLikes(postId: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)!!
            val isdisLiked = post.dislikedBy.contains(currentUserId)
            if (isdisLiked) {
                post.dislikedBy.remove(currentUserId)
            } else {
                if (post.likedBy.contains(currentUserId)){
                    post.likedBy.remove(currentUserId)
                }
                post.dislikedBy.add(currentUserId)
            }
            postCollections.document(postId).set(post)
        }
    }
}