package com.iboi.plano.model

enum class StatusAssinatura {
    TRIAL,      // Período de teste (30 dias)
    ATIVA,      // Assinatura ativa e paga
    VENCIDA,    // Assinatura venceu e precisa pagar
    CANCELADA,  // Cancelada pelo usuário
    SUSPENSA    // Suspensa por inadimplência
}
