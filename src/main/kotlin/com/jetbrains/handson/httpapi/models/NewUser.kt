package com.jetbrains.handson.httpapi.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

val users = mutableListOf<NewUser>()

@Serializable
data class NewUser(
    @BsonId
    val id: Int,
    val name: String,
    val age: Int
)
