package com.iboi.sanitario.repository

import com.iboi.sanitario.domain.ProtocoloSanitario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProtocoloSanitarioRepository : JpaRepository<ProtocoloSanitario, UUID> {
    fun findByFarmIdAndAtivo(farmId: UUID, ativo: Boolean): List<ProtocoloSanitario>
}
