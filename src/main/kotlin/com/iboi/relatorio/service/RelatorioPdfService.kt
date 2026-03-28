package com.iboi.relatorio.service

import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.relatorio.dto.DashboardResponse
import com.iboi.relatorio.dto.HistoricoAnimalResponse
import com.iboi.relatorio.dto.RelatorioRebanhoResponse
import com.iboi.relatorio.usecase.DashboardUseCase
import com.iboi.relatorio.usecase.HistoricoAnimalUseCase
import com.iboi.relatorio.usecase.RelatorioRebanhoUseCase
import com.iboi.rebanho.repository.AnimalRepository
import com.lowagie.text.Document
import com.lowagie.text.Font
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class RelatorioPdfService(
        private val farmRepository: FarmRepository,
        private val animalRepository: AnimalRepository,
        private val relatorioRebanhoUseCase: RelatorioRebanhoUseCase,
        private val dashboardUseCase: DashboardUseCase,
        private val historicoAnimalUseCase: HistoricoAnimalUseCase
) {

    private val titleFont = Font(Font.HELVETICA, 18f, Font.BOLD)
    private val sectionFont = Font(Font.HELVETICA, 13f, Font.BOLD)
    private val bodyFont = Font(Font.HELVETICA, 10f, Font.NORMAL)
    private val smallFont = Font(Font.HELVETICA, 9f, Font.NORMAL)
    private val generatedAtFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun exportarRelatorioFazendaPdf(farmId: UUID): ByteArray {
        val farm = farmRepository.findById(farmId).orElseThrow { IllegalArgumentException("Fazenda nao encontrada") }
        val rebanho = relatorioRebanhoUseCase.execute(farmId)
        val dashboard = dashboardUseCase.execute(farmId)

        val output = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 36f, 36f, 36f, 36f)
        PdfWriter.getInstance(document, output)
        document.open()

        adicionarCabecalho(
                document = document,
                titulo = "Relatorio da fazenda",
                subtitulo = "${farm.name} - ${farm.city}/${farm.state}"
        )

        adicionarParagrafo(document, "Resumo operacional", sectionFont)
        document.add(criarTabelaResumoFazenda(rebanho, dashboard))

        adicionarEspaco(document)
        adicionarParagrafo(document, "Rebanho por categoria", sectionFont)
        document.add(criarTabelaMapa("Categoria", "Quantidade", rebanho.porCategoria.mapKeys { it.key.toString() }))

        adicionarEspaco(document)
        adicionarParagrafo(document, "Rebanho por status", sectionFont)
        document.add(criarTabelaMapa("Status", "Quantidade", rebanho.porStatus.mapKeys { it.key.toString() }))

        adicionarEspaco(document)
        adicionarParagrafo(document, "Eventos recentes", sectionFont)
        if (dashboard.eventosRecentes.isEmpty()) {
            adicionarParagrafo(document, "Nenhum evento recente encontrado.", bodyFont)
        } else {
            document.add(criarTabelaEventos(dashboard))
        }

        adicionarEspaco(document)
        adicionarParagrafo(document, "Agendamentos proximos", sectionFont)
        if (dashboard.agendamentosProximos.isEmpty()) {
            adicionarParagrafo(document, "Nenhum agendamento proximo encontrado.", bodyFont)
        } else {
            document.add(criarTabelaAgendamentos(dashboard))
        }

        document.close()
        return output.toByteArray()
    }

    fun exportarHistoricoAnimalPdf(animalId: UUID): ByteArray {
        val animal = animalRepository.findById(animalId).orElseThrow { IllegalArgumentException("Animal nao encontrado") }
        val historico = historicoAnimalUseCase.execute(animalId)

        val output = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 36f, 36f, 36f, 36f)
        PdfWriter.getInstance(document, output)
        document.open()

        adicionarCabecalho(
                document = document,
                titulo = "Ficha e historico do animal",
                subtitulo = "Brinco ${animal.brinco}${animal.nome?.let { " - $it" } ?: ""}"
        )

        adicionarParagrafo(document, "Dados principais", sectionFont)
        document.add(
                criarTabelaLinhas(
                        listOf(
                                "Brinco" to animal.brinco,
                                "Nome" to (animal.nome ?: "-"),
                                "Raca" to animal.raca.toString(),
                                "Categoria" to animal.categoria.toString(),
                                "Status" to animal.status.toString(),
                                "Peso atual" to (animal.pesoAtual?.toPlainString()?.plus(" kg") ?: "-"),
                                "Pasto atual" to (animal.pasture?.name ?: "-"),
                                "Lote atual" to (animal.lote?.nome ?: "-"),
                                "SISBOV" to (animal.codigoSisbov ?: "-")
                        )
                )
        )

        adicionarEspaco(document)
        adicionarParagrafo(document, "Historico de eventos", sectionFont)
        if (historico.timeline.isEmpty()) {
            adicionarParagrafo(document, "Nenhum evento registrado para este animal.", bodyFont)
        } else {
            document.add(criarTabelaHistorico(historico))
        }

        adicionarEspaco(document)
        adicionarParagrafo(document, "Evolucao de peso", sectionFont)
        if (historico.evolucaoPeso.isEmpty()) {
            adicionarParagrafo(document, "Nao ha pesagens suficientes para este animal.", bodyFont)
        } else {
            document.add(criarTabelaPesos(historico))
        }

        document.close()
        return output.toByteArray()
    }

    private fun adicionarCabecalho(document: Document, titulo: String, subtitulo: String) {
        document.add(Paragraph("BovCore", Font(Font.HELVETICA, 11f, Font.BOLD)))
        document.add(Paragraph(titulo, titleFont))
        document.add(Paragraph(subtitulo, bodyFont))
        document.add(Paragraph("Gerado em ${LocalDateTime.now().format(generatedAtFormatter)}", smallFont))
        adicionarEspaco(document)
    }

    private fun criarTabelaResumoFazenda(rebanho: RelatorioRebanhoResponse, dashboard: DashboardResponse): PdfPTable {
        return criarTabelaLinhas(
                listOf(
                        "Animais ativos" to dashboard.kpis.totalAnimaisAtivos.toString(),
                        "Total de animais" to rebanho.totalAnimais.toString(),
                        "Nascimentos no mes" to dashboard.kpis.nascimentosMes.toString(),
                        "Mortes no mes" to dashboard.kpis.mortesMes.toString(),
                        "Peso medio" to (rebanho.pesoMedio?.toPlainString()?.plus(" kg") ?: "-"),
                        "Idade media" to "${rebanho.idadeMediaMeses} meses",
                        "Despesas do mes" to "R$ ${dashboard.kpis.despesasMes}"
                )
        )
    }

    private fun criarTabelaMapa(coluna1: String, coluna2: String, valores: Map<String, Long>): PdfPTable {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 1f))
        adicionarCabecalhoTabela(table, coluna1)
        adicionarCabecalhoTabela(table, coluna2)
        valores.forEach { (chave, valor) ->
            adicionarCelula(table, formatarRotulo(chave))
            adicionarCelula(table, valor.toString())
        }
        return table
    }

    private fun criarTabelaEventos(dashboard: DashboardResponse): PdfPTable {
        val table = PdfPTable(4)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1.4f, 1.6f, 1.4f, 3.6f))
        adicionarCabecalhoTabela(table, "Data")
        adicionarCabecalhoTabela(table, "Tipo")
        adicionarCabecalhoTabela(table, "Animal")
        adicionarCabecalhoTabela(table, "Descricao")
        dashboard.eventosRecentes.forEach {
            adicionarCelula(table, it.data.toString())
            adicionarCelula(table, formatarRotulo(it.tipo))
            adicionarCelula(table, it.animal)
            adicionarCelula(table, it.descricao)
        }
        return table
    }

    private fun criarTabelaAgendamentos(dashboard: DashboardResponse): PdfPTable {
        val table = PdfPTable(4)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1.4f, 1.6f, 1.4f, 2.6f))
        adicionarCabecalhoTabela(table, "Data")
        adicionarCabecalhoTabela(table, "Tipo")
        adicionarCabecalhoTabela(table, "Animal")
        adicionarCabecalhoTabela(table, "Produto")
        dashboard.agendamentosProximos.forEach {
            adicionarCelula(table, it.dataPrevista.toString())
            adicionarCelula(table, formatarRotulo(it.tipo))
            adicionarCelula(table, it.animal)
            adicionarCelula(table, it.produto)
        }
        return table
    }

    private fun criarTabelaHistorico(historico: HistoricoAnimalResponse): PdfPTable {
        val table = PdfPTable(3)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1.3f, 1.5f, 4.2f))
        adicionarCabecalhoTabela(table, "Data")
        adicionarCabecalhoTabela(table, "Tipo")
        adicionarCabecalhoTabela(table, "Descricao")
        historico.timeline.forEach {
            adicionarCelula(table, it.data.toString())
            adicionarCelula(table, formatarRotulo(it.tipo.toString()))
            adicionarCelula(table, it.descricao)
        }
        return table
    }

    private fun criarTabelaPesos(historico: HistoricoAnimalResponse): PdfPTable {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(2f, 1.2f))
        adicionarCabecalhoTabela(table, "Data")
        adicionarCabecalhoTabela(table, "Peso")
        historico.evolucaoPeso.forEach {
            adicionarCelula(table, it.data.toString())
            adicionarCelula(table, "${it.peso} kg")
        }
        return table
    }

    private fun criarTabelaLinhas(linhas: List<Pair<String, String>>): PdfPTable {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(2.2f, 3.8f))
        linhas.forEach { (label, value) ->
            adicionarCabecalhoTabela(table, label)
            adicionarCelula(table, value)
        }
        return table
    }

    private fun adicionarCabecalhoTabela(table: PdfPTable, value: String) {
        val cell = PdfPCell(Paragraph(value, Font(Font.HELVETICA, 10f, Font.BOLD)))
        cell.backgroundColor = java.awt.Color(238, 242, 255)
        cell.borderColor = java.awt.Color(220, 220, 220)
        cell.setPadding(8f)
        table.addCell(cell)
    }

    private fun adicionarCelula(table: PdfPTable, value: String) {
        val cell = PdfPCell(Paragraph(value, bodyFont))
        cell.borderColor = java.awt.Color(230, 230, 230)
        cell.setPadding(8f)
        table.addCell(cell)
    }

    private fun adicionarParagrafo(document: Document, texto: String, font: Font) {
        document.add(Paragraph(texto, font))
    }

    private fun adicionarEspaco(document: Document) {
        document.add(Paragraph(" ", bodyFont))
    }

    private fun formatarRotulo(value: String): String {
        return value.lowercase()
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { parte -> parte.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
    }
}
