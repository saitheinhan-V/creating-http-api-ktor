package com.jetbrains.handson.httpapi.routes

import com.jetbrains.handson.httpapi.models.NewUser
import com.jetbrains.handson.httpapi.models.customerStorage
import com.jetbrains.handson.httpapi.models.users
import io.ktor.application.*
import io.ktor.http.*
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
        delete("/delete/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if(id.isNotEmpty()){
                collection.deleteOne("{name:'$id'}")
//                if (users.removeIf { it.id == id.toInt() }) {
//                val user = collection.find().toList()
//                call.respond(user)
                    call.respondText("Customer removed correctly", status = HttpStatusCode.Accepted)
//                } else {
//                    call.respondText("User Not Found", status = HttpStatusCode.NotFound)
//                }
            } else {
                call.respondText("Empty id", status = HttpStatusCode.NotFound)
            }


        }
    }

}