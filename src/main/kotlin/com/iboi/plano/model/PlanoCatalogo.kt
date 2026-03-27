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
                    descricao = "Acesso temporario completo para conhecer a plataforma.",
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
            TipoAssinatura.FREE to PlanoDefinicao(
                    tipo = TipoAssinatura.FREE,
                    titulo = "Free",
                    descricao = "Entrada gratuita para iniciar a operacao e validar o produto.",
                    limiteAnimais = 50,
                    recursos = setOf(PlanoRecurso.CADASTRO_BASICO)
            ),
            TipoAssinatura.BASIC to PlanoDefinicao(
                    tipo = TipoAssinatura.BASIC,
                    titulo = "Basic",
                    descricao = "Cadastro completo com operacao de manejo e sanidade.",
                    limiteAnimais = null,
                    recursos = setOf(
                            PlanoRecurso.CADASTRO_BASICO,
                            PlanoRecurso.CADASTRO_COMPLETO,
                            PlanoRecurso.PESAGEM,
                            PlanoRecurso.VACINACAO,
                            PlanoRecurso.MOVIMENTACAO
                    )
            ),
            TipoAssinatura.PRO to PlanoDefinicao(
                    tipo = TipoAssinatura.PRO,
                    titulo = "Pro",
                    descricao = "Camada gerencial com relatorios e leitura economica da fazenda.",
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
                    descricao = "Plano decisorio com inteligencia preditiva e recomendacoes.",
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
