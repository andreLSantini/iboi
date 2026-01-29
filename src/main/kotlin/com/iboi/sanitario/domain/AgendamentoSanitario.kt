package com.iboi.sanitario.domain

import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.Evento
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
        name = "agendamentos_sanitarios",
        indexes = [
                Index(name = "idx_agendamento_animal", columnList = "animal_id"),
                Index(name = "idx_agendamento_data", columnList = "dataPrevista"),
                Index(name = "idx_agendamento_status", columnList = "status")
        ]
)
class AgendamentoSanitario(
        @Id
        @GeneratedValue
        val id: UUID? = null,

        @ManyToOne
        @JoinColumn(name = "animal_id", nullable = false)
        val animal: Animal,

        @ManyToOne
        @JoinColumn(name = "item_protocolo_id", nullable = false)
        val itemProtocolo: ItemProtocolo,

        @Column(nullable = false)
        var dataPrevista: LocalDate,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        var status: StatusAgendamento = StatusAgendamento.PENDENTE,

        @OneToOne
        @JoinColumn(name = "evento_id")
        var eventoRealizado: Evento? = null,

        @Column(nullable = false)
        val criadoEm: LocalDateTime = LocalDateTime.now(),

        @Column
        var atualizadoEm: LocalDateTime? = null
)
