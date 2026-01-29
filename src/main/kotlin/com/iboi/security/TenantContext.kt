package com.iboi.security

object TenantContext {
    private val current = ThreadLocal<String?>()

    fun set(tenant: String?) = current.set(tenant)
    fun get(): String? = current.get()
    fun clear() = current.remove()
}
