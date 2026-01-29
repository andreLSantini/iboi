package com.iboi.security

import java.util.*

object FarmContext {
    private val farm = ThreadLocal<UUID>()
    fun set(id: UUID) = farm.set(id)
    fun get(): UUID = farm.get()
    fun clear() = farm.remove()
}
