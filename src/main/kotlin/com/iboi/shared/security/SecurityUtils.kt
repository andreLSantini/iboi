package com.iboi.shared.security

import com.iboi.security.AuthenticatedUser
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

object SecurityUtils {

    private fun principal(): AuthenticatedUser =
            SecurityContextHolder.getContext().authentication?.principal as? AuthenticatedUser
                    ?: throw IllegalStateException("Usuário autenticado não encontrado")

    fun currentUserId(): UUID = principal().userId

    fun currentEmail(): String = principal().email

    fun currentEmpresaId(): UUID = principal().empresaId

    fun currentFarmId(): UUID =
            principal().farmId ?: throw IllegalStateException("Usuário não possui fazenda padrão no token")
}
