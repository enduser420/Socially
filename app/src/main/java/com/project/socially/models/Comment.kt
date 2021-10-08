package com.project.socially.models

data class Comment (
    val text: String = "",
    val createdBy: User = User(),
    val createdAt: Long = 0L,
    val likedBy: ArrayList<String> = ArrayList(),
    val dislikedBy: ArrayList<String> = ArrayList()
)