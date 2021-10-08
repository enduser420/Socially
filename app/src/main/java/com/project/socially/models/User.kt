package com.project.socially.models

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val imageUrl: String = "https://i.pinimg.com/736x/a9/7f/39/a97f3940c166321294859fc336a11829.jpg",
    val gender: String = "",
    val dob: String = "",
    val about: String = ""
)