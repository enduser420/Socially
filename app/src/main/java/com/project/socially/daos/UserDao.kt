package com.project.socially.daos

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.project.socially.models.User
import com.project.socially.utilities.USERS_REF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    private val db = FirebaseFirestore.getInstance()
    val usersCollection = db.collection(USERS_REF)

    fun addUser(user: User?) {
        user?.let {
            GlobalScope.launch(Dispatchers.IO) {
                usersCollection.document(user.uid).set(it)
            }
        }
    }

    fun getUserById(uId: String): Task<DocumentSnapshot> {
        return usersCollection.document(uId).get()
    }

    fun deleteUser(uId: String) {
        usersCollection.document(uId).delete()
    }

    fun updateUser(user: User?) {
        user?.let {
            GlobalScope.launch(Dispatchers.IO) {
                usersCollection.document(user.uid).set(it, SetOptions.merge())
            }
        }
    }

    fun updateUser(userId: String, photoUrl: String) {
        usersCollection.document(userId).update("imageUrl",photoUrl)
    }
}