package com.iboi.ia.usecase

import com.iboi.ia.repository.AlertaRepository
import com.iboi.ia.service.DetectorAlertasProdutivosService
import com.iboi.ia.service.DetectorAlertasSanitariosService
import com.iboi.identity.infrastructure.repository.FarmRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class GerarAlertasUseCase(
        private val farmRepository: FarmRepository,
        private val detectorSanitario: DetectorAlertasSanitariosService,
        private val detectorProdutivo: DetectorAlertasProdutivosService,
        private val alertaRepository: AlertaRepository
) {

    @Transactional
    fun execute(farmId: UUID): Int {
        val farm = farmRepository.findById(farmId).orElseThrow {
            IllegalArgumentException("Fazenda não encontrada")
        }

        val alertasSanitarios = detectorSanitario.detectar(farm)
        val alertasProdutivos = detectorProdutivo.detectar(farm)

        val todosAlertas = alertasSanitarios + alertasProdutivos

        todosAlertas.forEach { alertaRepository.save(it) }

        return todosAlertas.size
    }
}
