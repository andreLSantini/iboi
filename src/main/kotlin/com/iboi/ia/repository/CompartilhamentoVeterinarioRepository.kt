package com.iboi.ia.repository

import com.iboi.ia.domain.CompartilhamentoVeterinario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CompartilhamentoVeterinarioRepository : JpaRepository<CompartilhamentoVeterinario, UUID> {
    fun findByFarmIdAndAtivo(farmId: UUID, ativo: Boolean): List<CompartilhamentoVeterinario>
    fun findByTokenAcesso(token: String): CompartilhamentoVeterinario?
}
