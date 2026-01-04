package com.iboi.auth.repository

import com.iboi.auth.model.Pessoa
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PessoaRepository : JpaRepository<Pessoa, UUID> {
    fun findByDocumento(documento: String): Pessoa?
}
