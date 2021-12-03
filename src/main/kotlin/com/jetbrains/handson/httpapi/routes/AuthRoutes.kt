package com.jetbrains.handson.httpapi.routes

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.jetbrains.handson.httpapi.models.User
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit


fun Application.registerAuthRoutes(){
    routing {
        login()
    }
}

fun Route.login(){
    route("/auth"){
//        post("/login") {
//            val user = call.receive<User>()
//            // Check username and password
//            // ...
//            val publicKey = jwkProvider.get("6f8856ed-9189-488f-9011-0ff4b6c08edc").publicKey
//            val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
//            val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
//            val token = JWT.create()
//                .withAudience(audience)
//                .withIssuer(issuer)
//                .withClaim("username", user.username)
//                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
//                .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
//            call.respond(hashMapOf("token" to token))
//        }


        authenticate("auth-jwt") {
            get("/hello") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
            }
        }
        static(".well-known") {
            staticRootFolder = File("certs")
            file("jwks.json")
        }
    }
}