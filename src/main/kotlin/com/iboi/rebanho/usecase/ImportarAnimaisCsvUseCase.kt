package com.iboi.rebanho.usecase

import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.PastureRepository
import com.iboi.plano.service.PlanoAcessoService
import com.iboi.rebanho.api.dto.ImportarAnimaisResponse
import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.OrigemAnimal
import com.iboi.rebanho.domain.Raca
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.LoteRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.UUID

@Component
class ImportarAnimaisCsvUseCase(
        private val animalRepository: AnimalRepository,
        private val farmRepository: FarmRepository,
        private val loteRepository: LoteRepository,
        private val pastureRepository: PastureRepository,
        private val planoAcessoService: PlanoAcessoService
) {

    @Transactional
    fun execute(farmId: UUID, file: MultipartFile): ImportarAnimaisResponse {
        require(!file.isEmpty) { "Arquivo CSV obrigatorio" }

        val farm = farmRepository.findById(farmId).orElseThrow {
            IllegalArgumentException("Fazenda nao encontrada")
        }

        val reader = BufferedReader(InputStreamReader(file.inputStream, StandardCharsets.UTF_8))
        val lines = reader.readLines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            throw IllegalArgumentException("Arquivo CSV vazio")
        }

        planoAcessoService.requireCapacidadeAnimais(farm.empresa.id!!, (lines.size - 1).coerceAtLeast(0))

        val delimiter = if (lines.first().count { it == ';' } >= lines.first().count { it == ',' }) ';' else ','
        val headers = lines.first().split(delimiter).map { normalizeHeader(it) }

        val erros = mutableListOf<String>()
        var importados = 0
        var ignorados = 0

        lines.drop(1).forEachIndexed { index, line ->
            val numeroLinha = index + 2
            val values = line.split(delimiter)

            if (values.all { it.isBlank() }) {
                ignorados++
                return@forEachIndexed
            }

            val row = headers.mapIndexed { headerIndex, header ->
                header to values.getOrNull(headerIndex).orEmpty().trim()
            }.toMap()

            try {
                val brinco = row.valueOf("brinco", numeroLinha)
                if (animalRepository.existsByBrincoAndFarmId(brinco, farmId)) {
                    ignorados++
                    return@forEachIndexed
                }

                val rfid = row["rfid"]?.takeIf { it.isNotBlank() }
                if (!rfid.isNullOrBlank() && animalRepository.existsByRfidAndFarmId(rfid, farmId)) {
                    throw IllegalArgumentException("RFID duplicado na fazenda")
                }

                val codigoSisbov = row["codigosisbov"]?.takeIf { it.isNotBlank() }
                        ?: row["sisbov"]?.takeIf { it.isNotBlank() }
                if (!codigoSisbov.isNullOrBlank() && animalRepository.existsByCodigoSisbov(codigoSisbov)) {
                    throw IllegalArgumentException("Codigo SISBOV ja cadastrado")
                }

                val lote = row["lote"]?.takeIf { it.isNotBlank() }?.let {
                    loteRepository.findByFarmIdAndNomeIgnoreCase(farmId, it)
                            ?: throw IllegalArgumentException("Lote '$it' nao encontrado")
                }

                val pasture = row["pasto"]?.takeIf { it.isNotBlank() }?.let {
                    pastureRepository.findByFarmIdAndNameIgnoreCase(farmId, it)
                            ?: throw IllegalArgumentException("Pasto '$it' nao encontrado")
                }

                animalRepository.save(
                        Animal(
                                brinco = brinco,
                                rfid = rfid,
                                codigoSisbov = codigoSisbov,
                                nome = row["nome"]?.takeIf { it.isNotBlank() },
                                sexo = parseSexo(row.valueOf("sexo", numeroLinha)),
                                raca = parseRaca(row.valueOf("raca", numeroLinha)),
                                dataNascimento = LocalDate.parse(row.valueOf("datanascimento", numeroLinha)),
                                pesoAtual = row["pesoatual"]?.takeIf { it.isNotBlank() }?.replace(",", ".")?.let { BigDecimal(it) },
                                categoria = parseCategoria(row.valueOf("categoria", numeroLinha)),
                                origem = row["origem"]?.takeIf { it.isNotBlank() }?.let { parseOrigem(it) } ?: OrigemAnimal.NASCIMENTO,
                                farm = farm,
                                lote = lote,
                                pasture = pasture,
                                observacoes = row["observacoes"]?.takeIf { it.isNotBlank() },
                                dataEntrada = row["dataentrada"]?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) },
                                sisbovAtivo = row["sisbovativo"]?.equals("true", ignoreCase = true) == true ||
                                        row["sisbovativo"]?.equals("sim", ignoreCase = true) == true,
                                status = StatusAnimal.ATIVO
                        )
                )
                importados++
            } catch (ex: Exception) {
                erros.add("Linha $numeroLinha: ${ex.message ?: "erro ao importar"}")
            }
        }

        return ImportarAnimaisResponse(
                totalLinhas = (lines.size - 1).coerceAtLeast(0),
                importados = importados,
                ignorados = ignorados,
                erros = erros
        )
    }

    private fun normalizeHeader(value: String): String =
            value.trim()
                    .lowercase()
                    .replace(" ", "")
                    .replace("_", "")
                    .replace("-", "")
                    .replace("ã", "a")
                    .replace("á", "a")
                    .replace("â", "a")
                    .replace("é", "e")
                    .replace("ê", "e")
                    .replace("í", "i")
                    .replace("ó", "o")
                    .replace("ô", "o")
                    .replace("õ", "o")
                    .replace("ú", "u")
                    .replace("ç", "c")

    private fun Map<String, String>.valueOf(key: String, line: Int): String =
            this[key]?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("coluna '$key' obrigatoria na linha $line")

    private fun parseSexo(value: String): Sexo = when (normalizeHeader(value)) {
        "macho", "m" -> Sexo.MACHO
        "femea", "f", "femea?" -> Sexo.FEMEA
        else -> throw IllegalArgumentException("sexo invalido: $value")
    }

    private fun parseCategoria(value: String): CategoriaAnimal {
        val normalized = normalizeHeader(value)
        return CategoriaAnimal.entries.firstOrNull { normalizeHeader(it.name) == normalized }
                ?: throw IllegalArgumentException("categoria invalida: $value")
    }

    private fun parseRaca(value: String): Raca {
        val normalized = normalizeHeader(value)
        return Raca.entries.firstOrNull { normalizeHeader(it.name) == normalized }
                ?: throw IllegalArgumentException("raca invalida: $value")
    }

    private fun parseOrigem(value: String): OrigemAnimal = when (normalizeHeader(value)) {
        "nascimento" -> OrigemAnimal.NASCIMENTO
        "compra", "comprado" -> OrigemAnimal.COMPRA
        else -> throw IllegalArgumentException("origem invalida: $value")
    }
}
