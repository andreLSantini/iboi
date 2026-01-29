package com.iboi.rebanho.domain

enum class TipoEvento {
    VACINA,         // Vacinação
    VERMIFUGO,      // Vermifugação
    PESAGEM,        // Pesagem do animal
    MOVIMENTACAO,   // Movimentação entre lotes
    NASCIMENTO,     // Nascimento
    DESMAME,        // Desmame
    MORTE,          // Morte/óbito
    VENDA,          // Venda do animal
    COMPRA,         // Compra/entrada de animal
    TRATAMENTO,     // Tratamento veterinário
    INSEMINACAO,    // Inseminação artificial
    COBERTURA,      // Cobertura (monta natural)
    PARTO,          // Parto
    DIAGNOSTICO_GESTACAO, // Diagnóstico de gestação
    DESCARTE,       // Descarte/refugo
    OBSERVACAO      // Observação geral
}
