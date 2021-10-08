package com.project.socially.daos

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.project.socially.models.Comment
import com.project.socially.utilities.COMMENT_REF
import com.project.socially.utilities.POST_REF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommentDao {
    private val db = FirebaseFirestore.getInstance()
    private val postCollections = db.collection(POST_REF)
    private val auth = Firebase.auth

    /*fun addComment(postId: String, text: String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime = System.currentTimeMillis()
            val comment = Comment(text, user, currentTime)
            postCollections.document(postId).collection(COMMENT_REF).document().set(comment)
        }
    }*/

    private fun getCommentById(postId: String, commentId: String): Task<DocumentSnapshot> {
        return postCollections.document(postId).collection(COMMENT_REF).document(commentId).get()
    }

    fun commentupdateLikes(postId: String, commentId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val currentUserId = auth.currentUser!!.uid
            val comment =
                getCommentById(postId, commentId).await().toObject(Comment::class.java)
            println(comment)
            val isLiked = comment!!.likedBy.contains(currentUserId)

            if (isLiked) {
                comment.likedBy.remove(currentUserId)
            } else {
                if (comment.dislikedBy.contains(currentUserId)) {
                    comment.dislikedBy.remove(currentUserId)
                }
                comment.likedBy.add(currentUserId)
            }
            postCollections.document(postId).collection(COMMENT_REF).document(commentId)
                .set(comment)
        }
    }

    fun commentupdatedisLikes(postId: String, commentId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val currentUserId = auth.currentUser!!.uid
            val comment =
                getCommentById(postId, commentId).await().toObject(Comment::class.java)
            println(comment)
            val isdisLiked = comment!!.dislikedBy.contains(currentUserId)

            if (isdisLiked) {
                comment.dislikedBy.remove(currentUserId)
            } else {
                if (comment.likedBy.contains(currentUserId)) {
                    comment.likedBy.remove(currentUserId)
                }
                comment.dislikedBy.add(currentUserId)
            }
            postCollections.document(postId).collection(COMMENT_REF).document(commentId)
                .set(comment)
        }
    }

    fun updateComment(postId: String, commentId: String, newComment: String) {
        GlobalScope.launch {
            val oldComment = getCommentById(postId, commentId).await().toObject(Comment::class.java)!!
            val user = oldComment.createdBy
            val liked = oldComment.likedBy
            val disliked = oldComment.dislikedBy
            val currentTime = oldComment.createdAt
            val comment = Comment(newComment, user, currentTime, liked, disliked)
            postCollections.document(postId).collection(COMMENT_REF).document(commentId).set(comment)
        }
    }
}