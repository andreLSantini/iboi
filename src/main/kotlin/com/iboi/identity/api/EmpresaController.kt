package com.iboi.identity.api

import com.iboi.identity.api.dto.request.AtualizarEmpresaRequest
import com.iboi.identity.api.dto.response.EmpresaDto
import com.iboi.identity.infrastructure.repository.EmpresaRepository
import com.iboi.shared.security.SecurityUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/empresa")
class EmpresaController(
        private val empresaRepository: EmpresaRepository
) {

    @GetMapping("/minha")
    fun minha(): ResponseEntity<EmpresaDto> {
        val empresa = empresaRepository.findById(SecurityUtils.currentEmpresaId())
                .orElseThrow { IllegalArgumentException("Empresa nao encontrada") }

        return ResponseEntity.ok(empresa.toDto())
    }

    @PutMapping("/minha")
    fun atualizarMinha(@RequestBody request: AtualizarEmpresaRequest): ResponseEntity<EmpresaDto> {
        if (request.nome.isBlank()) {
            throw IllegalArgumentException("Nome da empresa e obrigatorio")
        }

        val empresa = empresaRepository.findById(SecurityUtils.currentEmpresaId())
                .orElseThrow { IllegalArgumentException("Empresa nao encontrada") }

        empresa.nome = request.nome.trim()
        empresa.cnpj = request.cnpj?.trim()?.ifBlank { null }

        return ResponseEntity.ok(empresaRepository.save(empresa).toDto())
    }

    private fun com.iboi.identity.domain.Empresa.toDto() = EmpresaDto(
            id = id!!,
            nome = nome,
            tipo = tipo,
            cnpj = cnpj,
            asaasCustomerId = asaasCustomerId,
            ativa = ativa
    )
}
