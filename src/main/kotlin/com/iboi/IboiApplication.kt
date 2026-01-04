package com.iboi

import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class IboiApplication

fun main(args: Array<String>) {
	runApplication<IboiApplication>(*args)
}
