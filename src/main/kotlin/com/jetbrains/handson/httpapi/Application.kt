package com.jetbrains.handson.httpapi

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.jetbrains.handson.httpapi.models.NewUser
import com.jetbrains.handson.httpapi.models.User
import com.jetbrains.handson.httpapi.models.users
import com.jetbrains.handson.httpapi.routes.registerAuthRoutes
import com.jetbrains.handson.httpapi.routes.registerCustomerRoutes
import com.jetbrains.handson.httpapi.routes.registerNewUser
import com.jetbrains.handson.httpapi.routes.registerOrderRoutes
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.utils.EmptyContent.status
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)



fun Application.module() {

    val client = KMongo.createClient().coroutine
    val database = client.getDatabase("users")
    val col = database.getCollection<NewUser>()

    install(ContentNegotiation) {
        json()
        gson()

    }
    install(CORS) {
        anyHost()
    }

    val secret = environment.config.property("ktor.jwt.secret").getString()
    val issuer = environment.config.property("ktor.jwt.issuer").getString()
    val audience = environment.config.property("ktor.jwt.audience").getString()
    val myRealm = environment.config.property("ktor.jwt.realm").getString()
    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build())
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    //registerAuthRoutes()
    registerNewUser(col)

    routing {
        post("/login") {
            call.parameters
            val user = call.receive<NewUser>()
            // Check username and password
            val checkUser = col.findOne("{phone:'${user.phone}'}")
//            val checkUser = col.findOneById(user.id)

            // ...
            if(checkUser != null && checkUser.id != 0) {
                val token = JWT.create()
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .withClaim("name", checkUser.id)
                    .withClaim("phone", checkUser.phone)
                    .withExpiresAt(Date(System.currentTimeMillis() + 60000000))
                    .sign(Algorithm.HMAC256(secret))
                call.respond(hashMapOf("token" to token))
            }else{
                call.respondText("No user found", status = HttpStatusCode.NotFound)
            }
        }

        authenticate("auth-jwt") {
            get("/hello") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("name").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
            }
            registerCustomerRoutes()
            registerOrderRoutes()
        }
    }
}
