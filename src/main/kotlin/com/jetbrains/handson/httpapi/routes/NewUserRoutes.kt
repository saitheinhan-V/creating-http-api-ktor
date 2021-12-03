package com.jetbrains.handson.httpapi.routes

import com.jetbrains.handson.httpapi.models.NewUser
import com.jetbrains.handson.httpapi.models.users
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.coroutine.CoroutineCollection

fun Application.registerNewUser(collection: CoroutineCollection<NewUser>) {

    routing {
        newUserRouting(collection)
    }
    // your code
}

fun Route.newUserRouting(collection: CoroutineCollection<NewUser>){
    route("/user"){
        get("/users") {
            val users = collection.find().toList()
            call.respond(users)
        }
        post {
            call.parameters
            val requestBody = call.receive<NewUser>()
            val user = requestBody.copy(id = users.size.plus(1))
            users.add(user)
            call.respond(user)
        }
        post("/users") {
            call.parameters
            val requestBody = call.receive<NewUser>()
            val isSuccess = collection.insertOne(requestBody).wasAcknowledged()
            call.respond(isSuccess)
        }

    }

}