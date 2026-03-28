package com.iboi.plano.model

data class PlanoDefinicao(
        val tipo: TipoAssinatura,
        val titulo: String,
        val descricao: String,
        val limiteAnimais: Int?,
        val recursos: Set<PlanoRecurso>
)

object PlanoCatalogo {

    private val definicoes = mapOf(
            TipoAssinatura.TRIAL to PlanoDefinicao(
                    tipo = TipoAssinatura.TRIAL,
                    titulo = "Trial",
                    descricao = "Acesso temporario ao nucleo operacional do produto para conhecer a plataforma antes da cobranca.",
                    limiteAnimais = 50,
                    recursos = setOf(
                            PlanoRecurso.CADASTRO_BASICO,
                            PlanoRecurso.CADASTRO_COMPLETO,
                            PlanoRecurso.PESAGEM,
                            PlanoRecurso.VACINACAO,
                            PlanoRecurso.MOVIMENTACAO,
                            PlanoRecurso.RELATORIOS
                    )
            ),
            TipoAssinatura.FREE to PlanoDefinicao(
                    tipo = TipoAssinatura.FREE,
                    titulo = "Trial legado",
                    descricao = "Plano legado mantido apenas para compatibilidade com contas antigas.",
                    limiteAnimais = 50,
                    recursos = setOf(
                            PlanoRecurso.CADASTRO_BASICO,
                            PlanoRecurso.CADASTRO_COMPLETO,
                            PlanoRecurso.PESAGEM,
                            PlanoRecurso.VACINACAO,
                            PlanoRecurso.MOVIMENTACAO,
                            PlanoRecurso.RELATORIOS
                    )
            ),
            TipoAssinatura.BASIC to PlanoDefinicao(
                    tipo = TipoAssinatura.BASIC,
                    titulo = "Basic",
                    descricao = "Operacao completa com manejo, sanidade e relatorios simples para a rotina da fazenda.",
                    limiteAnimais = null,
                    recursos = setOf(
                            PlanoRecurso.CADASTRO_BASICO,
                            PlanoRecurso.CADASTRO_COMPLETO,
                            PlanoRecurso.PESAGEM,
                            PlanoRecurso.VACINACAO,
                            PlanoRecurso.MOVIMENTACAO,
                            PlanoRecurso.RELATORIOS
                    )
            ),
            TipoAssinatura.PRO to PlanoDefinicao(
                    tipo = TipoAssinatura.PRO,
                    titulo = "Pro",
                    descricao = "Camada futura para leitura economica da fazenda.",
                    limiteAnimais = null,
                    recursos = setOf(
                            PlanoRecurso.CADASTRO_BASICO,
                            PlanoRecurso.CADASTRO_COMPLETO,
                            PlanoRecurso.PESAGEM,
                            PlanoRecurso.VACINACAO,
                            PlanoRecurso.MOVIMENTACAO,
                            PlanoRecurso.RELATORIOS,
                            PlanoRecurso.FINANCEIRO_POR_ANIMAL,
                            PlanoRecurso.CUSTO_POR_CABECA
                    )
            ),
            TipoAssinatura.PREMIUM to PlanoDefinicao(
                    tipo = TipoAssinatura.PREMIUM,
                    titulo = "Premium",
                    descricao = "Camada futura com inteligencia preditiva e recomendacoes.",
                    limiteAnimais = null,
                    recursos = setOf(
                            PlanoRecurso.CADASTRO_BASICO,
                            PlanoRecurso.CADASTRO_COMPLETO,
                            PlanoRecurso.PESAGEM,
                            PlanoRecurso.VACINACAO,
                            PlanoRecurso.MOVIMENTACAO,
                            PlanoRecurso.RELATORIOS,
                            PlanoRecurso.FINANCEIRO_POR_ANIMAL,
                            PlanoRecurso.CUSTO_POR_CABECA,
                            PlanoRecurso.IA_DECISAO
                    )
            ),
            TipoAssinatura.ENTERPRISE to PlanoDefinicao(
                    tipo = TipoAssinatura.ENTERPRISE,
                    titulo = "Enterprise",
                    descricao = "Conta corporativa e consultiva para operacoes maiores.",
                    limiteAnimais = null,
                    recursos = setOf(
                            PlanoRecurso.CADASTRO_BASICO,
                            PlanoRecurso.CADASTRO_COMPLETO,
                            PlanoRecurso.PESAGEM,
                            PlanoRecurso.VACINACAO,
                            PlanoRecurso.MOVIMENTACAO,
                            PlanoRecurso.RELATORIOS,
                            PlanoRecurso.FINANCEIRO_POR_ANIMAL,
                            PlanoRecurso.CUSTO_POR_CABECA,
                            PlanoRecurso.IA_DECISAO
                    )
            )
    )

    fun get(tipo: TipoAssinatura): PlanoDefinicao = definicoes[tipo]
            ?: error("Plano nao mapeado: $tipo")
}
