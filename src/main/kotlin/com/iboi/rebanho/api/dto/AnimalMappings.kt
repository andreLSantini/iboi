package com.iboi.rebanho.api.dto

import com.iboi.identity.domain.Farm
import com.iboi.identity.domain.Pasture
import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.Evento
import com.iboi.rebanho.domain.MovimentacaoAnimal
import com.iboi.rebanho.domain.VacinacaoAnimal
import java.time.LocalDate
import java.time.Period

fun Animal.toDto(): AnimalDto {
    val idade = Period.between(dataNascimento, LocalDate.now()).toTotalMonths().toInt()

    return AnimalDto(
            id = id!!,
            brinco = brinco,
            rfid = rfid,
            codigoSisbov = codigoSisbov,
            sisbovAtivo = sisbovAtivo,
            nome = nome,
            sexo = sexo,
            raca = raca,
            dataNascimento = dataNascimento,
            dataEntrada = dataEntrada,
            idade = idade,
            pesoAtual = pesoAtual,
            status = status,
            categoria = categoria,
            origem = origem,
            lote = lote?.let { LoteResumoDto(it.id!!, it.nome) },
            pasture = pasture?.toResumoDto(),
            pai = pai?.let { AnimalResumoDto(it.id!!, it.brinco, it.nome) },
            mae = mae?.let { AnimalResumoDto(it.id!!, it.brinco, it.nome) },
            observacoes = observacoes
    )
}

fun Pasture.toResumoDto(): PastureResumoDto = PastureResumoDto(
        id = id!!,
        nome = name
)

fun Farm.toResumoDto(): FarmResumoDto = FarmResumoDto(
        id = id!!,
        nome = name
)

fun Evento.toDto(): EventoDto = EventoDto(
        id = id!!,
        animal = AnimalResumoDto(
                id = animal.id!!,
                brinco = animal.brinco,
                nome = animal.nome
        ),
        tipo = tipo,
        data = data,
        descricao = descricao,
        peso = peso,
        produto = produto,
        dose = dose,
        unidadeMedida = unidadeMedida,
        loteDestino = loteDestino?.let { LoteResumoDto(it.id!!, it.nome) },
        valor = valor,
        reprodutorNome = reprodutorNome,
        protocoloReprodutivo = protocoloReprodutivo,
        diagnosticoPositivo = diagnosticoPositivo,
        dataPrevistaParto = dataPrevistaParto,
        observacaoReprodutiva = observacaoReprodutiva,
        responsavel = responsavel?.nome
)

fun MovimentacaoAnimal.toDto(): MovimentacaoAnimalDto = MovimentacaoAnimalDto(
        id = id!!,
        tipo = tipo,
        movimentadaEm = movimentadaEm,
        farmOrigem = farmOrigem?.toResumoDto(),
        farmDestino = farmDestino?.toResumoDto(),
        pastureOrigem = pastureOrigem?.toResumoDto(),
        pastureDestino = pastureDestino?.toResumoDto(),
        numeroGta = numeroGta,
        documentoExterno = documentoExterno,
        motivo = motivo,
        observacoes = observacoes,
        responsavel = responsavel?.nome
)

fun VacinacaoAnimal.toDto(): VacinacaoAnimalDto = VacinacaoAnimalDto(
        id = id!!,
        tipo = tipo,
        nomeVacina = nomeVacina,
        dose = dose,
        unidadeMedida = unidadeMedida,
        aplicadaEm = aplicadaEm,
        proximaDoseEm = proximaDoseEm,
        fabricante = fabricante,
        loteVacina = loteVacina,
        observacoes = observacoes,
        responsavel = responsavel?.nome
)
