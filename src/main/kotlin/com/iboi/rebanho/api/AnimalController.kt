package com.iboi.rebanho.api

import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import com.iboi.rebanho.api.dto.*
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.usecase.CadastrarAnimalUseCase
import com.iboi.rebanho.usecase.ListarAnimaisUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Period
import java.util.*

@RestController
@RequestMapping("/api/animais")
class AnimalController(
        private val animalRepository: AnimalRepository,
        private val usuarioRepository: UsuarioRepository,
        private val userFarmProfileRepository: UserFarmProfileRepository,
        private val cadastrarAnimalUseCase: CadastrarAnimalUseCase,
        private val listarAnimaisUseCase: ListarAnimaisUseCase
) {

    @PostMapping
    fun cadastrar(@RequestBody request: CadastrarAnimalRequest): ResponseEntity<AnimalDto> {
        val farmId = getFarmIdFromAuth()
        val animal = cadastrarAnimalUseCase.execute(farmId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(animal)
    }

    @GetMapping
    fun listar(
            @RequestParam(required = false) status: StatusAnimal?,
            @RequestParam(required = false) categoria: com.iboi.rebanho.domain.CategoriaAnimal?,
            @RequestParam(required = false) loteId: UUID?,
            @RequestParam(required = false) sexo: com.iboi.rebanho.domain.Sexo?
    ): ResponseEntity<List<AnimalDto>> {
        val farmId = getFarmIdFromAuth()
        val filtro = FiltrarAnimaisRequest(status, categoria, loteId, sexo)
        val animais = listarAnimaisUseCase.execute(farmId, filtro)
        return ResponseEntity.ok(animais)
    }

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: UUID): ResponseEntity<AnimalDto> {
        val farmId = getFarmIdFromAuth()
        val animal = animalRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()

        if (animal.farm.id != farmId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(toDto(animal))
    }

    @PutMapping("/{id}")
    fun atualizar(
            @PathVariable id: UUID,
            @RequestBody request: AtualizarAnimalRequest
    ): ResponseEntity<AnimalDto> {
        val farmId = getFarmIdFromAuth()
        val animal = animalRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()

        if (animal.farm.id != farmId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        request.nome?.let { animal.nome = it }
        request.raca?.let { animal.raca = it }
        request.pesoAtual?.let { animal.pesoAtual = it }
        request.categoria?.let { animal.categoria = it }
        request.status?.let { animal.status = it }
        request.observacoes?.let { animal.observacoes = it }
        animal.atualizadoEm = java.time.LocalDateTime.now()

        val atualizado = animalRepository.save(animal)
        return ResponseEntity.ok(toDto(atualizado))
    }

    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: UUID): ResponseEntity<Void> {
        val farmId = getFarmIdFromAuth()
        val animal = animalRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()

        if (animal.farm.id != farmId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        animalRepository.delete(animal)
        return ResponseEntity.noContent().build()
    }

    private fun getFarmIdFromAuth(): UUID {
        val email = SecurityContextHolder.getContext().authentication.principal as String
        val usuario = usuarioRepository.findByEmail(email)
                ?: throw IllegalStateException("Usuário não encontrado")

        // Buscar o perfil default do usuário
        val userFarmProfile = userFarmProfileRepository.findByUsuario_IdAndIsDefaultTrue(usuario.id!!)
                ?: userFarmProfileRepository.findByUsuario_Id(usuario.id!!)
                ?: throw IllegalStateException("Usuário não possui fazenda associada")

        return userFarmProfile.farm.id!!
    }

    private fun toDto(animal: com.iboi.rebanho.domain.Animal): AnimalDto {
        val idade = Period.between(animal.dataNascimento, java.time.LocalDate.now()).toTotalMonths().toInt()

        return AnimalDto(
                id = animal.id!!,
                brinco = animal.brinco,
                nome = animal.nome,
                sexo = animal.sexo,
                raca = animal.raca,
                dataNascimento = animal.dataNascimento,
                idade = idade,
                pesoAtual = animal.pesoAtual,
                status = animal.status,
                categoria = animal.categoria,
                lote = animal.lote?.let {
                    LoteResumoDto(it.id!!, it.nome)
                },
                pai = animal.pai?.let {
                    AnimalResumoDto(it.id!!, it.brinco, it.nome)
                },
                mae = animal.mae?.let {
                    AnimalResumoDto(it.id!!, it.brinco, it.nome)
                },
                observacoes = animal.observacoes
        )
    }
}
