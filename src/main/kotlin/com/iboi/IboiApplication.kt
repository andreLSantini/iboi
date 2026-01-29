package com.iboi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IboiApplication

fun main(args: Array<String>) {
	runApplication<IboiApplication>(*args)
}
