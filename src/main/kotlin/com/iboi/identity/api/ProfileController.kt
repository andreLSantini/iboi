package com.iboi.identity.api


import com.iboi.identity.application.usecase.CreateProfileUseCase
import com.iboi.identity.domain.Profile
import com.iboi.identity.infrastructure.repository.ProfileRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/profiles")
class ProfileController(
        private val createProfileUseCase: CreateProfileUseCase,
        private val profileRepository: ProfileRepository
) {

    // ========================
    // DTOs
    // ========================

    data class CreateProfileRequest(
            val name: String,
            val permissions: Set<String>
    )

    // ========================
    // ENDPOINTS
    // ========================

    /**
     * Criar um novo perfil
     */
    @PostMapping
    @PreAuthorize("hasAuthority('EDIT_CORE')")
    fun create(@RequestBody request: CreateProfileRequest): Profile {
        return createProfileUseCase.execute(
                name = request.name,
                permissionCodes = request.permissions
        )
    }

    /**
     * Listar todos os perfis
     */
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_CORE')")
    fun list(): List<Profile> =
            profileRepository.findAll()

    /**
     * Buscar perfil por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_CORE')")
    fun findById(@PathVariable id: UUID): Profile =
            profileRepository.findById(id)
                    .orElseThrow { RuntimeException("Profile not found") }

    /**
     * Remover perfil
     * (opcional – cuidado em produção)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_CORE')")
    fun delete(@PathVariable id: UUID) {
        if (!profileRepository.existsById(id)) {
            throw RuntimeException("Profile not found")
        }
        profileRepository.deleteById(id)
    }
}
