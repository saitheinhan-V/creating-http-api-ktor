package com.jetbrains.handson.httpapi.models

import kotlinx.serialization.Serializable

val userStorage = User("user","root")

@Serializable
data class User(val username: String,val password: String)
