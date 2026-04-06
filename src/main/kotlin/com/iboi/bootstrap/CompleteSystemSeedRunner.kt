package com.iboi.bootstrap

import com.iboi.financeiro.domain.CategoriaDespesa
import com.iboi.financeiro.domain.Despesa
import com.iboi.financeiro.domain.FormaPagamento
import com.iboi.financeiro.domain.StatusLancamentoFinanceiro
import com.iboi.financeiro.repository.DespesaRepository
import com.iboi.ia.domain.Alerta
import com.iboi.ia.domain.PrioridadeAlerta
import com.iboi.ia.domain.StatusAlerta
import com.iboi.ia.domain.TipoAlerta
import com.iboi.ia.repository.AlertaRepository
import com.iboi.identity.domain.Empresa
import com.iboi.identity.domain.EmpresaType
import com.iboi.identity.domain.Farm
import com.iboi.identity.domain.FarmModule
import com.iboi.identity.domain.FarmRole
import com.iboi.identity.domain.Pasture
import com.iboi.identity.domain.ProductionType
import com.iboi.identity.domain.RoleEnum
import com.iboi.identity.domain.UserFarmProfile
import com.iboi.identity.domain.Usuario
import com.iboi.identity.infrastructure.repository.EmpresaRepository
import com.iboi.identity.infrastructure.repository.FarmModuleRepository
import com.iboi.identity.infrastructure.repository.FarmRepository
import com.iboi.identity.infrastructure.repository.PastureRepository
import com.iboi.identity.infrastructure.repository.UserFarmProfileRepository
import com.iboi.identity.infrastructure.repository.UsuarioRepository
import com.iboi.plano.model.Assinatura
import com.iboi.plano.model.MetodoPagamento
import com.iboi.plano.model.Pagamento
import com.iboi.plano.model.PeriodoPagamento
import com.iboi.plano.model.StatusAssinatura
import com.iboi.plano.model.StatusPagamento
import com.iboi.plano.model.TipoAssinatura
import com.iboi.plano.repository.AssinaturaRepository
import com.iboi.plano.repository.PagamentoRepository
import com.iboi.rebanho.domain.Animal
import com.iboi.rebanho.domain.CategoriaAnimal
import com.iboi.rebanho.domain.Evento
import com.iboi.rebanho.domain.Lote
import com.iboi.rebanho.domain.MovimentacaoAnimal
import com.iboi.rebanho.domain.OrigemAnimal
import com.iboi.rebanho.domain.Raca
import com.iboi.rebanho.domain.Sexo
import com.iboi.rebanho.domain.StatusAnimal
import com.iboi.rebanho.domain.TipoEvento
import com.iboi.rebanho.domain.TipoMovimentacaoAnimal
import com.iboi.rebanho.domain.TipoVacina
import com.iboi.rebanho.domain.VacinacaoAnimal
import com.iboi.rebanho.repository.AnimalRepository
import com.iboi.rebanho.repository.EventoRepository
import com.iboi.rebanho.repository.LoteRepository
import com.iboi.rebanho.repository.MovimentacaoAnimalRepository
import com.iboi.rebanho.repository.VacinacaoAnimalRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(prefix = "app.seed", name = ["enabled"], havingValue = "true")
class CompleteSystemSeedRunner(
        private val empresaRepository: EmpresaRepository,
        private val usuarioRepository: UsuarioRepository,
        private val farmRepository: FarmRepository,
        private val pastureRepository: PastureRepository,
        private val userFarmProfileRepository: UserFarmProfileRepository,
        private val farmModuleRepository: FarmModuleRepository,
        private val loteRepository: LoteRepository,
        private val animalRepository: AnimalRepository,
        private val eventoRepository: EventoRepository,
        private val movimentacaoAnimalRepository: MovimentacaoAnimalRepository,
        private val vacinacaoAnimalRepository: VacinacaoAnimalRepository,
        private val despesaRepository: DespesaRepository,
        private val alertaRepository: AlertaRepository,
        private val assinaturaRepository: AssinaturaRepository,
        private val pagamentoRepository: PagamentoRepository,
        private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        val adminEmail = "demo@bovcore.com.br"
        if (usuarioRepository.existsByEmail(adminEmail)) {
            reconcileDemoBilling(adminEmail)
            logger.info("Seed completo ignorado: usuario {} ja existe.", adminEmail)
            return
        }

        val agora = LocalDateTime.now()
        val hoje = LocalDate.now()

        val empresa = empresaRepository.save(
                Empresa(
                        nome = "BovCore Demo Agro",
                        tipo = EmpresaType.MATRIZ,
                        cnpj = "47287799000150",
                        asaasCustomerId = "cus_demo_bovcore",
                        ativa = true
                )
        )

        val admin = usuarioRepository.save(
                Usuario(
                        nome = "Andre Demo",
                        email = adminEmail,
                        telefone = "(11) 99999-0001",
                        senhaHash = passwordEncoder.encode("bovcore123"),
                        roleEnum = RoleEnum.ADMIN,
                        empresa = empresa
                )
        )

        val operador = usuarioRepository.save(
                Usuario(
                        nome = "Maria Operacao",
                        email = "operacao@bovcore.com.br",
                        telefone = "(11) 99999-0002",
                        senhaHash = passwordEncoder.encode("bovcore123"),
                        roleEnum = RoleEnum.USER,
                        empresa = empresa
                )
        )

        val santaHelena = farmRepository.save(
                Farm(
                        name = "Fazenda Santa Helena",
                        city = "Rondonopolis",
                        state = "MT",
                        productionType = ProductionType.CORTE,
                        size = 1280.0,
                        ownerName = "Andre Demo",
                        ownerDocument = "47287799000150",
                        phone = "(66) 3333-1000",
                        email = "fazenda@santahelena.com",
                        addressLine = "Rodovia MT-270, km 18",
                        zipCode = "78700-000",
                        latitude = -16.4673,
                        longitude = -54.6372,
                        legalStatus = "Regular",
                        ccir = "CCIR-2026-001",
                        car = "CAR-MT-9981",
                        mainExploration = "Cria, recria e engorda",
                        estimatedCapacity = 1500,
                        grazingArea = 920.0,
                        legalReserveArea = 240.0,
                        appArea = 60.0,
                        productiveArea = 980.0,
                        empresa = empresa
                )
        )

        val boaVista = farmRepository.save(
                Farm(
                        name = "Fazenda Boa Vista",
                        city = "Jatai",
                        state = "GO",
                        productionType = ProductionType.MISTO,
                        size = 640.0,
                        ownerName = "Andre Demo",
                        ownerDocument = "47287799000150",
                        phone = "(64) 3333-2000",
                        email = "fazenda@boavista.com",
                        addressLine = "Estrada municipal sentido Serra Azul",
                        zipCode = "75800-000",
                        latitude = -17.8781,
                        longitude = -51.7171,
                        legalStatus = "Regular",
                        ccir = "CCIR-2026-002",
                        car = "CAR-GO-1123",
                        mainExploration = "Recria e apoio sanitário",
                        estimatedCapacity = 700,
                        grazingArea = 430.0,
                        legalReserveArea = 120.0,
                        appArea = 24.0,
                        productiveArea = 496.0,
                        empresa = empresa
                )
        )

        userFarmProfileRepository.saveAll(
                listOf(
                        UserFarmProfile(usuario = admin, farm = santaHelena, role = FarmRole.ADMIN, isDefault = true),
                        UserFarmProfile(usuario = admin, farm = boaVista, role = FarmRole.ADMIN, isDefault = false),
                        UserFarmProfile(usuario = operador, farm = santaHelena, role = FarmRole.OPERATOR, isDefault = true)
                )
        )

        createFarmModules(santaHelena, boaVista)

        val pastoMaternidade = pastureRepository.save(
                Pasture(
                        name = "Pasto Maternidade",
                        areaHa = 86.5,
                        latitude = -16.4620,
                        longitude = -54.6312,
                        notes = "Pasto de vacas paridas e bezerros ao pe.",
                        farm = santaHelena
                )
        )
        val pastoRecria = pastureRepository.save(
                Pasture(
                        name = "Pasto Recria Norte",
                        areaHa = 140.0,
                        latitude = -16.4688,
                        longitude = -54.6424,
                        notes = "Lotes de novilhas e novilhos em ganho moderado.",
                        farm = santaHelena
                )
        )
        val pastoEngorda = pastureRepository.save(
                Pasture(
                        name = "Pasto Engorda Sul",
                        areaHa = 122.3,
                        latitude = -16.4731,
                        longitude = -54.6450,
                        notes = "Animais em terminação com suplemento.",
                        farm = santaHelena
                )
        )
        val pastoQuarentena = pastureRepository.save(
                Pasture(
                        name = "Pasto Quarentena",
                        areaHa = 32.0,
                        latitude = -17.8799,
                        longitude = -51.7110,
                        notes = "Entrada externa e animais em observacao sanitária.",
                        farm = boaVista
                )
        )
        val pastoReserva = pastureRepository.save(
                Pasture(
                        name = "Pasto Reserva",
                        areaHa = 74.0,
                        latitude = -17.8751,
                        longitude = -51.7198,
                        notes = "Reserva estratégica para seca.",
                        farm = boaVista
                )
        )

        val loteMatrizes = loteRepository.save(Lote(nome = "Matrizes 2026", descricao = "Lote principal de vacas e matrizes", farm = santaHelena))
        val loteRecria = loteRepository.save(Lote(nome = "Recria Machos", descricao = "Animais em recria com foco em GMD", farm = santaHelena))
        val loteEngorda = loteRepository.save(Lote(nome = "Engorda Intensiva", descricao = "Terminação de machos", farm = santaHelena))
        val loteQuarentena = loteRepository.save(Lote(nome = "Entrada Externa", descricao = "Animais recém chegados", farm = boaVista))

        val animal1 = animalRepository.save(
                Animal(
                        brinco = "BC-1001",
                        rfid = "RFID-0001001",
                        codigoSisbov = "SISBOV-1001",
                        nome = "Aurora",
                        sexo = Sexo.FEMEA,
                        raca = Raca.NELORE,
                        dataNascimento = hoje.minusYears(5),
                        pesoAtual = BigDecimal("468.50"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.MATRIZ,
                        origem = OrigemAnimal.NASCIMENTO,
                        farm = santaHelena,
                        lote = loteMatrizes,
                        pasture = pastoMaternidade,
                        observacoes = "Matriz de alta fertilidade.",
                        dataEntrada = hoje.minusYears(4),
                        sisbovAtivo = true
                )
        )
        val animal2 = animalRepository.save(
                Animal(
                        brinco = "BC-1002",
                        rfid = "RFID-0001002",
                        codigoSisbov = "SISBOV-1002",
                        nome = "Rubi",
                        sexo = Sexo.FEMEA,
                        raca = Raca.ANGUS,
                        dataNascimento = hoje.minusYears(3),
                        pesoAtual = BigDecimal("421.80"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.NOVILHA,
                        origem = OrigemAnimal.NASCIMENTO,
                        farm = santaHelena,
                        lote = loteRecria,
                        pasture = pastoRecria,
                        observacoes = "Novilha de reposição com boa resposta sanitária.",
                        dataEntrada = hoje.minusYears(2),
                        sisbovAtivo = false
                )
        )
        val animal3 = animalRepository.save(
                Animal(
                        brinco = "BC-1003",
                        rfid = "RFID-0001003",
                        codigoSisbov = "SISBOV-1003",
                        nome = "Titan",
                        sexo = Sexo.MACHO,
                        raca = Raca.NELORE,
                        dataNascimento = hoje.minusYears(2),
                        pesoAtual = BigDecimal("512.40"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.NOVILHO,
                        origem = OrigemAnimal.NASCIMENTO,
                        farm = santaHelena,
                        lote = loteEngorda,
                        pasture = pastoEngorda,
                        observacoes = "Animal próximo de ponto de venda.",
                        dataEntrada = hoje.minusMonths(18),
                        sisbovAtivo = true
                )
        )
        val animal4 = animalRepository.save(
                Animal(
                        brinco = "BC-1004",
                        rfid = "RFID-0001004",
                        codigoSisbov = "SISBOV-1004",
                        nome = "Marfim",
                        sexo = Sexo.MACHO,
                        raca = Raca.CRUZAMENTO_INDUSTRIAL,
                        dataNascimento = hoje.minusYears(1),
                        pesoAtual = BigDecimal("284.30"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.BEZERRO,
                        origem = OrigemAnimal.NASCIMENTO,
                        farm = santaHelena,
                        lote = loteRecria,
                        pasture = pastoMaternidade,
                        observacoes = "Bezerro em desmame assistido.",
                        dataEntrada = hoje.minusYears(1),
                        sisbovAtivo = false
                )
        )
        val animal5 = animalRepository.save(
                Animal(
                        brinco = "BC-2001",
                        rfid = "RFID-0002001",
                        codigoSisbov = "SISBOV-2001",
                        nome = "Guapa",
                        sexo = Sexo.FEMEA,
                        raca = Raca.BRAHMAN,
                        dataNascimento = hoje.minusYears(4),
                        pesoAtual = BigDecimal("447.20"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.VACA,
                        origem = OrigemAnimal.COMPRA,
                        farm = boaVista,
                        lote = loteQuarentena,
                        pasture = pastoQuarentena,
                        observacoes = "Animal recebido recentemente.",
                        dataEntrada = hoje.minusDays(18),
                        sisbovAtivo = true
                )
        )
        val animal6 = animalRepository.save(
                Animal(
                        brinco = "BC-2002",
                        rfid = "RFID-0002002",
                        codigoSisbov = "SISBOV-2002",
                        nome = "Atlas",
                        sexo = Sexo.MACHO,
                        raca = Raca.SENEPOL,
                        dataNascimento = hoje.minusYears(3),
                        pesoAtual = BigDecimal("533.90"),
                        status = StatusAnimal.TRANSFERIDO,
                        categoria = CategoriaAnimal.BOI,
                        origem = OrigemAnimal.COMPRA,
                        farm = boaVista,
                        lote = loteQuarentena,
                        pasture = pastoReserva,
                        observacoes = "Animal transferido para parceiro comercial.",
                        dataEntrada = hoje.minusMonths(8),
                        sisbovAtivo = true
                )
        )

        eventoRepository.saveAll(
                listOf(
                        Evento(
                                animal = animal1,
                                farm = santaHelena,
                                tipo = TipoEvento.PESAGEM,
                                data = hoje.minusDays(20),
                                descricao = "Pesagem de rotina da matriz Aurora.",
                                peso = BigDecimal("468.50"),
                                responsavel = admin
                        ),
                        Evento(
                                animal = animal2,
                                farm = santaHelena,
                                tipo = TipoEvento.VACINA,
                                data = hoje.minusDays(14),
                                descricao = "Aplicação de reforço sanitário na novilha Rubi.",
                                produto = "Vacina reprodutiva",
                                dose = BigDecimal("2.00"),
                                unidadeMedida = "mL",
                                responsavel = operador
                        ),
                        Evento(
                                animal = animal3,
                                farm = santaHelena,
                                tipo = TipoEvento.PESAGEM,
                                data = hoje.minusDays(7),
                                descricao = "Pesagem pré-venda do Titan.",
                                peso = BigDecimal("512.40"),
                                valor = BigDecimal("17.80"),
                                responsavel = admin
                        ),
                        Evento(
                                animal = animal4,
                                farm = santaHelena,
                                tipo = TipoEvento.DESMAME,
                                data = hoje.minusDays(12),
                                descricao = "Desmame controlado com suplementação inicial.",
                                responsavel = operador
                        ),
                        Evento(
                                animal = animal5,
                                farm = boaVista,
                                tipo = TipoEvento.COMPRA,
                                data = hoje.minusDays(18),
                                descricao = "Entrada do animal Guapa para observação e integração.",
                                valor = BigDecimal("8200.00"),
                                responsavel = admin
                        ),
                        Evento(
                                animal = animal6,
                                farm = boaVista,
                                tipo = TipoEvento.MOVIMENTACAO,
                                data = hoje.minusDays(4),
                                descricao = "Transferência do animal Atlas para parceiro externo.",
                                responsavel = admin
                        )
                )
        )

        vacinacaoAnimalRepository.saveAll(
                listOf(
                        VacinacaoAnimal(
                                animal = animal1,
                                farm = santaHelena,
                                tipo = TipoVacina.AFTOSA,
                                nomeVacina = "Aftobov Premium",
                                dose = BigDecimal("2.00"),
                                unidadeMedida = "mL",
                                aplicadaEm = hoje.minusDays(32),
                                proximaDoseEm = hoje.plusMonths(5),
                                fabricante = "VetSaude",
                                loteVacina = "AFT-2026-01",
                                responsavel = operador
                        ),
                        VacinacaoAnimal(
                                animal = animal3,
                                farm = santaHelena,
                                tipo = TipoVacina.CLOSTRIDIOSE,
                                nomeVacina = "Clostrimax",
                                dose = BigDecimal("5.00"),
                                unidadeMedida = "mL",
                                aplicadaEm = hoje.minusDays(21),
                                proximaDoseEm = hoje.plusMonths(11),
                                fabricante = "Biogado",
                                loteVacina = "CLO-2026-08",
                                responsavel = operador
                        ),
                        VacinacaoAnimal(
                                animal = animal5,
                                farm = boaVista,
                                tipo = TipoVacina.RAIVA,
                                nomeVacina = "Raivax",
                                dose = BigDecimal("2.00"),
                                unidadeMedida = "mL",
                                aplicadaEm = hoje.minusDays(10),
                                proximaDoseEm = hoje.plusMonths(12),
                                fabricante = "VetSaude",
                                loteVacina = "RAV-2026-03",
                                responsavel = admin
                        )
                )
        )

        movimentacaoAnimalRepository.saveAll(
                listOf(
                        MovimentacaoAnimal(
                                animal = animal4,
                                tipo = TipoMovimentacaoAnimal.ENTRE_PASTOS,
                                farmOrigem = santaHelena,
                                farmDestino = santaHelena,
                                pastureOrigem = pastoMaternidade,
                                pastureDestino = pastoRecria,
                                movimentadaEm = hoje.minusDays(11),
                                motivo = "Entrada em recria após desmame.",
                                observacoes = "Mudança acompanhada por lote de adaptação.",
                                responsavel = operador
                        ),
                        MovimentacaoAnimal(
                                animal = animal6,
                                tipo = TipoMovimentacaoAnimal.SAIDA_EXTERNA,
                                farmOrigem = boaVista,
                                farmDestino = null,
                                pastureOrigem = pastoReserva,
                                pastureDestino = null,
                                movimentadaEm = hoje.minusDays(4),
                                numeroGta = "GTA-2026-4412",
                                motivo = "Transferência comercial.",
                                observacoes = "Saída programada para parceiro de terminação.",
                                responsavel = admin
                        )
                )
        )

        despesaRepository.saveAll(
                listOf(
                        Despesa(
                                farm = santaHelena,
                                categoria = CategoriaDespesa.ALIMENTACAO,
                                descricao = "Suplemento proteico do lote de engorda",
                                valor = BigDecimal("4820.00"),
                                data = hoje.minusDays(8),
                                formaPagamento = FormaPagamento.PIX,
                                dataVencimento = hoje.minusDays(6),
                                dataLiquidacao = hoje.minusDays(6),
                                status = StatusLancamentoFinanceiro.PAGO,
                                lote = loteEngorda,
                                responsavel = operador,
                                observacoes = "Compra mensal para 60 dias."
                        ),
                        Despesa(
                                farm = santaHelena,
                                categoria = CategoriaDespesa.MEDICAMENTOS,
                                descricao = "Lote de vacinas e seringas",
                                valor = BigDecimal("1360.00"),
                                data = hoje.minusDays(15),
                                formaPagamento = FormaPagamento.BOLETO,
                                dataVencimento = hoje.minusDays(10),
                                status = StatusLancamentoFinanceiro.VENCIDO,
                                animal = animal1,
                                responsavel = operador
                        ),
                        Despesa(
                                farm = boaVista,
                                categoria = CategoriaDespesa.TRANSPORTE,
                                descricao = "Frete de entrada dos animais da quarentena",
                                valor = BigDecimal("2280.00"),
                                data = hoje.minusDays(19),
                                formaPagamento = FormaPagamento.TRANSFERENCIA,
                                dataVencimento = hoje.minusDays(17),
                                dataLiquidacao = hoje.minusDays(17),
                                status = StatusLancamentoFinanceiro.PAGO,
                                lote = loteQuarentena,
                                responsavel = admin
                        ),
                        Despesa(
                                farm = boaVista,
                                categoria = CategoriaDespesa.VETERINARIO,
                                descricao = "Consulta e protocolo de observação sanitária",
                                valor = BigDecimal("890.00"),
                                data = hoje.minusDays(9),
                                formaPagamento = FormaPagamento.PIX,
                                dataVencimento = hoje.plusDays(2),
                                status = StatusLancamentoFinanceiro.PENDENTE,
                                animal = animal5,
                                responsavel = admin
                        )
                )
        )

        alertaRepository.saveAll(
                listOf(
                        Alerta(
                                farm = santaHelena,
                                tipo = TipoAlerta.SEM_PESAGEM_RECENTE,
                                prioridade = PrioridadeAlerta.MEDIA,
                                titulo = "Atualizar peso do lote de recria",
                                mensagem = "A novilha Rubi está há mais de 30 dias sem pesagem registrada.",
                                animal = animal2,
                                recomendacao = "Agendar pesagem na próxima passada pelo curral."
                        ),
                        Alerta(
                                farm = santaHelena,
                                tipo = TipoAlerta.RECOMENDACAO_VENDA,
                                prioridade = PrioridadeAlerta.ALTA,
                                titulo = "Titan próximo do ponto ideal de venda",
                                mensagem = "O animal Titan já atingiu o peso-alvo da janela de terminação.",
                                animal = animal3,
                                recomendacao = "Simular venda e validar margem do lote de engorda."
                        ),
                        Alerta(
                                farm = boaVista,
                                tipo = TipoAlerta.ANIMAL_RISCO_SANITARIO,
                                prioridade = PrioridadeAlerta.CRITICA,
                                titulo = "Monitorar quarentena da Guapa",
                                mensagem = "Animal com entrada recente exige atenção sanitária e revisão do protocolo.",
                                animal = animal5,
                                recomendacao = "Confirmar reforço vacinal e repetir inspeção clínica em 48h.",
                                status = StatusAlerta.ATIVO
                        )
                )
        )

        val assinatura = assinaturaRepository.save(
                Assinatura(
                        empresa = empresa,
                        tipo = TipoAssinatura.BASIC,
                        status = StatusAssinatura.ATIVA,
                        periodoPagamento = PeriodoPagamento.MENSAL,
                        dataInicio = agora.minusMonths(2),
                        dataVencimento = agora.plusDays(18),
                        proximaCobranca = agora.plusDays(18),
                        valor = BigDecimal("79.00"),
                        asaasSubscriptionId = "sub_demo_basic_001"
                )
        )

        pagamentoRepository.saveAll(
                listOf(
                        Pagamento(
                                assinatura = assinatura,
                                valor = BigDecimal("79.00"),
                                dataVencimento = agora.minusDays(42),
                                dataPagamento = agora.minusDays(40),
                                status = StatusPagamento.PAGO,
                                metodoPagamento = MetodoPagamento.PIX,
                                transacaoId = "pay_demo_001_pago",
                                gatewayProvider = "asaas-demo",
                                asaasSubscriptionId = "sub_demo_basic_001",
                                invoiceUrl = "https://demo.bovcore.local/faturas/001",
                                pixPayload = "00020126580014BR.GOV.BCB.PIX0114bovcore-demo520400005303986540679.005802BR5925BOVCORE DEMO AGRO6009SAO PAULO62070503***6304ABCD"
                        ),
                        Pagamento(
                                assinatura = assinatura,
                                valor = BigDecimal("79.00"),
                                dataVencimento = agora.minusDays(12),
                                dataPagamento = agora.minusDays(10),
                                status = StatusPagamento.PAGO,
                                metodoPagamento = MetodoPagamento.PIX,
                                transacaoId = "pay_demo_002_pago",
                                gatewayProvider = "asaas-demo",
                                asaasSubscriptionId = "sub_demo_basic_001",
                                invoiceUrl = "https://demo.bovcore.local/faturas/002",
                                pixPayload = "00020126580014BR.GOV.BCB.PIX0114bovcore-demo520400005303986540679.005802BR5925BOVCORE DEMO AGRO6009SAO PAULO62070503***6304EFGH"
                        )
                )
        )

        logger.info("Seed completo BovCore criado com sucesso.")
        logger.info("Login demo: {} / bovcore123", adminEmail)
        logger.info("Login operação: operacao@bovcore.com.br / bovcore123")
    }

    private fun createFarmModules(vararg farms: Farm) {
        val modules = listOf("REBANHO", "SANITARIO", "FINANCEIRO", "RELATORIOS", "IA")
        farms.forEach { farm ->
            modules.forEach { moduleCode ->
                if (!farmModuleRepository.existsByFarmIdAndModuleCodeAndActiveTrue(farm.id!!, moduleCode)) {
                    farmModuleRepository.save(
                            FarmModule(
                                    farmId = farm.id,
                                    moduleCode = moduleCode,
                                    active = true
                            )
                    )
                }
            }
        }
    }

    private fun reconcileDemoBilling(adminEmail: String) {
        val usuario = usuarioRepository.findByEmail(adminEmail) ?: return
        val empresa = usuario.empresa
        val agora = LocalDateTime.now()

        val assinatura = assinaturaRepository.findByEmpresaId(empresa.id!!)
                ?: assinaturaRepository.save(
                        Assinatura(
                                empresa = empresa,
                                tipo = TipoAssinatura.BASIC,
                                status = StatusAssinatura.ATIVA,
                                periodoPagamento = PeriodoPagamento.MENSAL,
                                dataInicio = agora.minusMonths(2),
                                dataVencimento = agora.plusDays(18),
                                proximaCobranca = agora.plusDays(18),
                                valor = BigDecimal("79.00"),
                                asaasSubscriptionId = "sub_demo_basic_001"
                        )
                )

        assinatura.tipo = TipoAssinatura.BASIC
        assinatura.status = StatusAssinatura.ATIVA
        assinatura.periodoPagamento = PeriodoPagamento.MENSAL
        assinatura.dataVencimento = agora.plusDays(18)
        assinatura.proximaCobranca = agora.plusDays(18)
        assinatura.valor = BigDecimal("79.00")
        assinatura.asaasSubscriptionId = assinatura.asaasSubscriptionId ?: "sub_demo_basic_001"
        assinaturaRepository.save(assinatura)

        val pagamentosExistentes = pagamentoRepository.findByAssinaturaEmpresaIdOrderByDataVencimentoDesc(empresa.id!!)
        if (pagamentosExistentes.isNotEmpty()) {
            pagamentoRepository.deleteAll(pagamentosExistentes)
        }

        pagamentoRepository.saveAll(
                listOf(
                        Pagamento(
                                assinatura = assinatura,
                                valor = BigDecimal("79.00"),
                                dataVencimento = agora.minusDays(42),
                                dataPagamento = agora.minusDays(40),
                                status = StatusPagamento.PAGO,
                                metodoPagamento = MetodoPagamento.PIX,
                                transacaoId = "pay_demo_001_pago",
                                gatewayProvider = "asaas-demo",
                                asaasSubscriptionId = assinatura.asaasSubscriptionId,
                                invoiceUrl = "https://demo.bovcore.local/faturas/001"
                        ),
                        Pagamento(
                                assinatura = assinatura,
                                valor = BigDecimal("79.00"),
                                dataVencimento = agora.minusDays(12),
                                dataPagamento = agora.minusDays(10),
                                status = StatusPagamento.PAGO,
                                metodoPagamento = MetodoPagamento.PIX,
                                transacaoId = "pay_demo_002_pago",
                                gatewayProvider = "asaas-demo",
                                asaasSubscriptionId = assinatura.asaasSubscriptionId,
                                invoiceUrl = "https://demo.bovcore.local/faturas/002"
                        )
                )
        )
        logger.info("Estado de cobranca do demo reconciliado para assinatura adimplente.")
    }
}
