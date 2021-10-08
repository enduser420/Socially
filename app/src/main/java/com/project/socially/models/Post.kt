package com.project.socially.models

data class Post (
    val text: String = "",
    val createdBy: User = User(),
    val createdAt: Long = 0L,
    val photoUrl: String = "",
    val likedBy: ArrayList<String> = ArrayList(),
    val dislikedBy: ArrayList<String> = ArrayList()
)