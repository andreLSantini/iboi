package com.iboi.rebanho.api.dto

data class ImportarAnimaisResponse(
        val totalLinhas: Int,
        val importados: Int,
        val ignorados: Int,
        val erros: List<String>
)
