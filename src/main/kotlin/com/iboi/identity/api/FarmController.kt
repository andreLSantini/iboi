package com.iboi.identity.api

import com.iboi.identity.api.dto.request.AtualizarFazendaRequest
import com.iboi.identity.api.dto.request.CadastrarFazendaRequest
import com.iboi.identity.api.dto.request.CadastrarPastoRequest
import com.iboi.identity.api.dto.request.SelecionarFazendaRequest
import com.iboi.identity.api.dto.response.FarmDetailDto
import com.iboi.identity.api.dto.response.FarmSummaryDto
import com.iboi.identity.api.dto.response.LoginResponse
import com.iboi.identity.api.dto.response.PastureDto
import com.iboi.identity.application.usecase.BuildAuthResponseUseCase
import com.iboi.identity.application.usecase.CadastrarFazendaUseCase
import com.iboi.identity.domain.Farm
import com.iboi.identity.domain.Pasture
import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.PastureRepository
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import com.iboi.shared.security.SecurityUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PutMapping
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/farms")
class FarmController(
        private val userFarmProfileRepository: UserFarmProfileRepository,
        private val buildAuthResponseUseCase: BuildAuthResponseUseCase,
        private val cadastrarFazendaUseCase: CadastrarFazendaUseCase,
        private val farmRepository: FarmRepository,
        private val pastureRepository: PastureRepository
) {

    @GetMapping
    fun listMyFarms(): ResponseEntity<List<FarmSummaryDto>> {
        val farms = userFarmProfileRepository.findAllByUsuario_Id(SecurityUtils.currentUserId())
                .map {
                    val farm = it.farm
                    FarmSummaryDto(
                            id = farm.id!!,
                            name = farm.name,
                            city = farm.city,
                            state = farm.state,
                            productionType = farm.productionType.name,
                            size = farm.size,
                            active = farm.active,
                            pastureCount = pastureRepository.findByFarmIdOrderByNameAsc(farm.id).size
                    )
                }

        return ResponseEntity.ok(farms)
    }

    @GetMapping("/{farmId}")
    fun getFarm(@PathVariable farmId: UUID): ResponseEntity<FarmDetailDto> {
        val farm = requireFarmAccess(farmId)
        return ResponseEntity.ok(farm.toDetailDto())
    }

    @PostMapping
    fun createFarm(@RequestBody request: CadastrarFazendaRequest): ResponseEntity<FarmSummaryDto> {
        val farm = cadastrarFazendaUseCase.execute(
                userId = SecurityUtils.currentUserId(),
                empresaId = SecurityUtils.currentEmpresaId(),
                request = request
        )

        return ResponseEntity.ok(farm)
    }

    @PostMapping("/select")
    fun selectFarm(@RequestBody request: SelecionarFazendaRequest): ResponseEntity<LoginResponse> {
        val response = buildAuthResponseUseCase.execute(
                userId = SecurityUtils.currentUserId(),
                selectedFarmId = request.farmId
        )

        return ResponseEntity.ok(response)
    }

    @PutMapping("/{farmId}")
    fun updateFarm(
            @PathVariable farmId: UUID,
            @RequestBody request: AtualizarFazendaRequest
    ): ResponseEntity<FarmDetailDto> {
        val farm = requireFarmAccess(farmId)

        farm.name = request.nome.trim()
        farm.city = request.cidade.trim()
        farm.state = request.estado.trim().uppercase()
        farm.productionType = request.tipoProducao
        farm.size = request.tamanho
        farm.ownerName = request.ownerName?.trim()
        farm.ownerDocument = request.ownerDocument?.trim()
        farm.phone = request.phone?.trim()
        farm.email = request.email?.trim()
        farm.addressLine = request.addressLine?.trim()
        farm.zipCode = request.zipCode?.trim()
        farm.latitude = request.latitude
        farm.longitude = request.longitude
        farm.legalStatus = request.legalStatus?.trim()
        farm.documentProof = request.documentProof?.trim()
        farm.ccir = request.ccir?.trim()
        farm.cib = request.cib?.trim()
        farm.car = request.car?.trim()
        farm.mainExploration = request.mainExploration?.trim()
        farm.estimatedCapacity = request.estimatedCapacity
        farm.grazingArea = request.grazingArea
        farm.legalReserveArea = request.legalReserveArea
        farm.appArea = request.appArea
        farm.productiveArea = request.productiveArea
        farm.updatedAt = LocalDateTime.now()

        return ResponseEntity.ok(farmRepository.save(farm).toDetailDto())
    }

    @GetMapping("/{farmId}/pastures")
    fun listPastures(@PathVariable farmId: UUID): ResponseEntity<List<PastureDto>> {
        requireFarmAccess(farmId)
        val pastures = pastureRepository.findByFarmIdOrderByNameAsc(farmId).map { it.toDto() }
        return ResponseEntity.ok(pastures)
    }

    @PostMapping("/{farmId}/pastures")
    fun createPasture(
            @PathVariable farmId: UUID,
            @RequestBody request: CadastrarPastoRequest
    ): ResponseEntity<PastureDto> {
        val farm = requireFarmAccess(farmId)
        require(request.nome.isNotBlank()) { "Nome do pasto é obrigatório" }

        val pasture = pastureRepository.save(
                Pasture(
                        name = request.nome.trim(),
                        areaHa = request.areaHa,
                        latitude = request.latitude,
                        longitude = request.longitude,
                        geoJson = request.geoJson?.trim(),
                        notes = request.notes?.trim(),
                        farm = farm
                )
        )

        return ResponseEntity.ok(pasture.toDto())
    }

    private fun requireFarmAccess(farmId: UUID): Farm {
        userFarmProfileRepository.findByUsuario_IdAndFarm_Id(SecurityUtils.currentUserId(), farmId)
                ?: throw IllegalArgumentException("Usuário não possui acesso a esta fazenda")

        return farmRepository.findByIdAndEmpresa_Id(farmId, SecurityUtils.currentEmpresaId())
                ?: throw IllegalArgumentException("Fazenda não encontrada")
    }

    private fun Farm.toDetailDto() = FarmDetailDto(
            id = id!!,
            name = name,
            city = city,
            state = state,
            productionType = productionType,
            size = size,
            ownerName = ownerName,
            ownerDocument = ownerDocument,
            phone = phone,
            email = email,
            addressLine = addressLine,
            zipCode = zipCode,
            latitude = latitude,
            longitude = longitude,
            legalStatus = legalStatus,
            documentProof = documentProof,
            ccir = ccir,
            cib = cib,
            car = car,
            mainExploration = mainExploration,
            estimatedCapacity = estimatedCapacity,
            grazingArea = grazingArea,
            legalReserveArea = legalReserveArea,
            appArea = appArea,
            productiveArea = productiveArea,
            active = active
    )

    private fun Pasture.toDto() = PastureDto(
            id = id!!,
            name = name,
            areaHa = areaHa,
            latitude = latitude,
            longitude = longitude,
            geoJson = geoJson,
            notes = notes,
            active = active
    )
}
