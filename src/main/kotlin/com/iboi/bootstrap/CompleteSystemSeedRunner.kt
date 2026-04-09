package com.iboi.bootstrap

import com.iboi.financeiro.domain.CategoriaDespesa
import com.iboi.financeiro.domain.Despesa
import com.iboi.financeiro.domain.FormaPagamento
import com.iboi.financeiro.domain.Receita
import com.iboi.financeiro.domain.StatusLancamentoFinanceiro
import com.iboi.financeiro.domain.TipoReceita
import com.iboi.financeiro.repository.DespesaRepository
import com.iboi.financeiro.repository.ReceitaRepository
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
        private val receitaRepository: ReceitaRepository,
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
                        nome = "BovCore Demo Master",
                        email = adminEmail,
                        telefone = "(11) 99999-0001",
                        senhaHash = passwordEncoder.encode("bovcore123"),
                        roleEnum = RoleEnum.ADMIN,
                        empresa = empresa
                )
        )

        val operador = usuarioRepository.save(
                Usuario(
                        nome = "Maria Operacao Campo",
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
        val loteElite = loteRepository.save(Lote(nome = "Elite Genetica", descricao = "Matrizes e reprodutores com destaque genetico", farm = santaHelena))
        val loteReserva = loteRepository.save(Lote(nome = "Reserva Seca", descricao = "Lote estrategico para contingencia e suporte", farm = boaVista))

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
        val animal7 = animalRepository.save(
                Animal(
                        brinco = "BC-1005",
                        rfid = "RFID-0001005",
                        codigoSisbov = "SISBOV-1005",
                        nome = "Comodoro",
                        sexo = Sexo.MACHO,
                        raca = Raca.ANGUS,
                        dataNascimento = hoje.minusYears(6),
                        pesoAtual = BigDecimal("811.30"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.TOURO,
                        origem = OrigemAnimal.COMPRA,
                        farm = santaHelena,
                        lote = loteElite,
                        pasture = pastoMaternidade,
                        observacoes = "Reprodutor destaque da bateria 2025/2026.",
                        dataEntrada = hoje.minusYears(2),
                        sisbovAtivo = true
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
        val animal8 = animalRepository.save(
                Animal(
                        brinco = "BC-1006",
                        rfid = "RFID-0001006",
                        codigoSisbov = "SISBOV-1006",
                        nome = "Safira",
                        sexo = Sexo.FEMEA,
                        raca = Raca.NELORE,
                        dataNascimento = hoje.minusMonths(10),
                        pesoAtual = BigDecimal("236.80"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.BEZERRO,
                        origem = OrigemAnimal.NASCIMENTO,
                        farm = santaHelena,
                        lote = loteMatrizes,
                        pasture = pastoMaternidade,
                        pai = animal7,
                        mae = animal1,
                        observacoes = "Bezerra filha de matriz destaque, usada para demonstracao de genealogia.",
                        dataEntrada = hoje.minusMonths(10),
                        sisbovAtivo = true
                )
        )
        val animal9 = animalRepository.save(
                Animal(
                        brinco = "BC-1007",
                        rfid = "RFID-0001007",
                        codigoSisbov = "SISBOV-1007",
                        nome = "Falcao",
                        sexo = Sexo.MACHO,
                        raca = Raca.SENEPOL,
                        dataNascimento = hoje.minusYears(2).minusMonths(4),
                        pesoAtual = BigDecimal("548.10"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.BOI,
                        origem = OrigemAnimal.COMPRA,
                        farm = santaHelena,
                        lote = loteEngorda,
                        pasture = pastoEngorda,
                        observacoes = "Animal de terminacao com desempenho acima da media do lote.",
                        dataEntrada = hoje.minusMonths(9),
                        sisbovAtivo = true
                )
        )
        val animal10 = animalRepository.save(
                Animal(
                        brinco = "BC-2003",
                        rfid = "RFID-0002003",
                        codigoSisbov = "SISBOV-2003",
                        nome = "Brisa",
                        sexo = Sexo.FEMEA,
                        raca = Raca.BRAHMAN,
                        dataNascimento = hoje.minusYears(5),
                        pesoAtual = BigDecimal("472.60"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.MATRIZ,
                        origem = OrigemAnimal.COMPRA,
                        farm = boaVista,
                        lote = loteReserva,
                        pasture = pastoReserva,
                        observacoes = "Matriz estabilizada na fazenda de apoio e em preparo reprodutivo.",
                        dataEntrada = hoje.minusMonths(14),
                        sisbovAtivo = true
                )
        )
        val animal11 = animalRepository.save(
                Animal(
                        brinco = "BC-2004",
                        rfid = "RFID-0002004",
                        codigoSisbov = "SISBOV-2004",
                        nome = "Orion",
                        sexo = Sexo.MACHO,
                        raca = Raca.CRUZAMENTO_INDUSTRIAL,
                        dataNascimento = hoje.minusYears(1).minusMonths(8),
                        pesoAtual = BigDecimal("391.40"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.NOVILHO,
                        origem = OrigemAnimal.COMPRA,
                        farm = boaVista,
                        lote = loteQuarentena,
                        pasture = pastoQuarentena,
                        observacoes = "Animal de entrada recente, usado para vitrine de quarentena e adaptacao.",
                        dataEntrada = hoje.minusDays(11),
                        sisbovAtivo = false
                )
        )
        val animal12 = animalRepository.save(
                Animal(
                        brinco = "BC-2005",
                        rfid = "RFID-0002005",
                        codigoSisbov = "SISBOV-2005",
                        nome = "Gaia",
                        sexo = Sexo.FEMEA,
                        raca = Raca.GUZERA,
                        dataNascimento = hoje.minusYears(2).minusMonths(2),
                        pesoAtual = BigDecimal("344.20"),
                        status = StatusAnimal.ATIVO,
                        categoria = CategoriaAnimal.NOVILHA,
                        origem = OrigemAnimal.COMPRA,
                        farm = boaVista,
                        lote = loteReserva,
                        pasture = pastoReserva,
                        observacoes = "Novilha selecionada para reposicao, com historico sanitario exemplar.",
                        dataEntrada = hoje.minusMonths(7),
                        sisbovAtivo = true
                )
        )

        animal4.pai = animal7
        animal4.mae = animal1
        animalRepository.save(animal4)

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

        eventoRepository.saveAll(
                listOf(
                        Evento(
                                animal = animal1,
                                farm = santaHelena,
                                tipo = TipoEvento.COBERTURA,
                                data = hoje.minusDays(96),
                                descricao = "Cobertura natural com touro Comodoro.",
                                reprodutorNome = "Comodoro",
                                protocoloReprodutivo = "Monta controlada 2026",
                                dataPrevistaParto = hoje.plusDays(189),
                                observacaoReprodutiva = "Cio observado e confirmado em curral.",
                                responsavel = admin
                        ),
                        Evento(
                                animal = animal1,
                                farm = santaHelena,
                                tipo = TipoEvento.DIAGNOSTICO_GESTACAO,
                                data = hoje.minusDays(52),
                                descricao = "Diagnostico de gestacao positivo.",
                                diagnosticoPositivo = true,
                                dataPrevistaParto = hoje.plusDays(189),
                                observacaoReprodutiva = "Prenhez confirmada em ultrassom.",
                                responsavel = admin
                        ),
                        Evento(
                                animal = animal10,
                                farm = boaVista,
                                tipo = TipoEvento.INSEMINACAO,
                                data = hoje.minusDays(38),
                                descricao = "IATF concluida na matriz Brisa.",
                                reprodutorNome = "Semen Elite Lote 14",
                                protocoloReprodutivo = "IATF protocolo 12 dias",
                                dataPrevistaParto = hoje.plusDays(247),
                                observacaoReprodutiva = "Sem intercorrencias no protocolo.",
                                responsavel = admin
                        ),
                        Evento(
                                animal = animal10,
                                farm = boaVista,
                                tipo = TipoEvento.DIAGNOSTICO_GESTACAO,
                                data = hoje.minusDays(6),
                                descricao = "Diagnostico recente ainda pendente de confirmacao final.",
                                observacaoReprodutiva = "Retornar em 14 dias para nova avaliacao.",
                                responsavel = admin
                        ),
                        Evento(
                                animal = animal8,
                                farm = santaHelena,
                                tipo = TipoEvento.NASCIMENTO,
                                data = hoje.minusMonths(10),
                                descricao = "Nascimento da bezerra Safira com parto assistido leve.",
                                peso = BigDecimal("31.40"),
                                responsavel = operador
                        ),
                        Evento(
                                animal = animal9,
                                farm = santaHelena,
                                tipo = TipoEvento.PESAGEM,
                                data = hoje.minusDays(18),
                                descricao = "Pesagem intermediaria de desempenho do Falcao.",
                                peso = BigDecimal("531.20"),
                                responsavel = operador
                        ),
                        Evento(
                                animal = animal9,
                                farm = santaHelena,
                                tipo = TipoEvento.PESAGEM,
                                data = hoje.minusDays(3),
                                descricao = "Pesagem recente do Falcao para curva de engorda.",
                                peso = BigDecimal("548.10"),
                                responsavel = admin
                        ),
                        Evento(
                                animal = animal11,
                                farm = boaVista,
                                tipo = TipoEvento.COMPRA,
                                data = hoje.minusDays(11),
                                descricao = "Entrada do animal Orion com conferencia documental completa.",
                                valor = BigDecimal("8420.00"),
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

        vacinacaoAnimalRepository.saveAll(
                listOf(
                        VacinacaoAnimal(
                                animal = animal8,
                                farm = santaHelena,
                                tipo = TipoVacina.CLOSTRIDIOSE,
                                nomeVacina = "Clostridiose Plus",
                                dose = BigDecimal("2.00"),
                                unidadeMedida = "mL",
                                aplicadaEm = hoje.minusDays(21),
                                proximaDoseEm = hoje.plusDays(9),
                                fabricante = "BioCampo",
                                loteVacina = "CLS-2026-21",
                                observacoes = "Primeiro reforco da bezerra Safira.",
                                responsavel = operador
                        ),
                        VacinacaoAnimal(
                                animal = animal10,
                                farm = boaVista,
                                tipo = TipoVacina.IBR_BVD,
                                nomeVacina = "Repro Shield",
                                dose = BigDecimal("5.00"),
                                unidadeMedida = "mL",
                                aplicadaEm = hoje.minusDays(43),
                                proximaDoseEm = hoje.plusMonths(6),
                                fabricante = "VetSaude",
                                loteVacina = "REP-2026-11",
                                observacoes = "Cobertura reprodutiva para protocolo IATF.",
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

        movimentacaoAnimalRepository.saveAll(
                listOf(
                        MovimentacaoAnimal(
                                animal = animal9,
                                tipo = TipoMovimentacaoAnimal.ENTRE_LOTES,
                                farmOrigem = santaHelena,
                                farmDestino = santaHelena,
                                pastureOrigem = pastoRecria,
                                pastureDestino = pastoEngorda,
                                movimentadaEm = hoje.minusDays(27),
                                motivo = "Promocao para lote de engorda apos bater meta de recria.",
                                observacoes = "Ajuste de manejo com lote alvo de terminacao.",
                                responsavel = operador
                        ),
                        MovimentacaoAnimal(
                                animal = animal11,
                                tipo = TipoMovimentacaoAnimal.ENTRADA_EXTERNA,
                                farmOrigem = boaVista,
                                farmDestino = boaVista,
                                pastureOrigem = null,
                                pastureDestino = pastoQuarentena,
                                movimentadaEm = hoje.minusDays(11),
                                numeroGta = "GTA-2026-7721",
                                motivo = "Entrada de compra para recria monitorada.",
                                observacoes = "Animal recebido com conferencia documental e inspeção clinica.",
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

        despesaRepository.saveAll(
                listOf(
                        Despesa(
                                farm = santaHelena,
                                categoria = CategoriaDespesa.REPRODUCAO,
                                descricao = "Protocolo hormonal das matrizes elite",
                                valor = BigDecimal("2140.00"),
                                data = hoje.minusDays(41),
                                formaPagamento = FormaPagamento.CARTAO_CREDITO,
                                dataVencimento = hoje.minusDays(35),
                                dataLiquidacao = hoje.minusDays(34),
                                status = StatusLancamentoFinanceiro.PAGO,
                                lote = loteElite,
                                responsavel = admin,
                                observacoes = "Investimento em IATF e manejo reprodutivo premium."
                        ),
                        Despesa(
                                farm = boaVista,
                                categoria = CategoriaDespesa.ALIMENTACAO,
                                descricao = "Silagem e suplemento mineral da reserva seca",
                                valor = BigDecimal("3180.00"),
                                data = hoje.minusDays(6),
                                formaPagamento = FormaPagamento.BOLETO,
                                dataVencimento = hoje.plusDays(7),
                                status = StatusLancamentoFinanceiro.PENDENTE,
                                lote = loteReserva,
                                responsavel = operador,
                                observacoes = "Reposicao antecipada para janela seca."
                        )
                )
        )

        receitaRepository.saveAll(
                listOf(
                        Receita(
                                farm = santaHelena,
                                tipo = TipoReceita.VENDA_ANIMAL,
                                descricao = "Venda individual do lote premium para frigorifico regional",
                                valor = BigDecimal("12480.00"),
                                data = hoje.minusDays(13),
                                dataVencimento = hoje.minusDays(11),
                                dataLiquidacao = hoje.minusDays(10),
                                formaPagamento = FormaPagamento.PIX,
                                status = StatusLancamentoFinanceiro.RECEBIDO,
                                animal = animal3,
                                responsavel = admin,
                                comprador = "Frigorifico Serra Dourada",
                                quantidadeAnimais = 1,
                                observacoes = "Margem acima da meta prevista para o ciclo."
                        ),
                        Receita(
                                farm = santaHelena,
                                tipo = TipoReceita.BONIFICACAO,
                                descricao = "Bonificacao por qualidade de carcaca e padrao de entrega",
                                valor = BigDecimal("1860.00"),
                                data = hoje.minusDays(9),
                                dataVencimento = hoje.minusDays(7),
                                dataLiquidacao = hoje.minusDays(7),
                                formaPagamento = FormaPagamento.TRANSFERENCIA,
                                status = StatusLancamentoFinanceiro.RECEBIDO,
                                lote = loteEngorda,
                                responsavel = admin,
                                comprador = "Frigorifico Serra Dourada",
                                observacoes = "Premio comercial vinculado ao fechamento de lote."
                        ),
                        Receita(
                                farm = boaVista,
                                tipo = TipoReceita.VENDA_LOTE,
                                descricao = "Venda parcial de lote de apoio para parceiro de recria",
                                valor = BigDecimal("28650.00"),
                                data = hoje.minusDays(4),
                                dataVencimento = hoje.plusDays(3),
                                formaPagamento = FormaPagamento.BOLETO,
                                status = StatusLancamentoFinanceiro.PENDENTE,
                                lote = loteReserva,
                                responsavel = admin,
                                comprador = "Agropecuaria Horizonte",
                                quantidadeAnimais = 6,
                                observacoes = "Titulo ainda em aberto para demonstracao de contas a receber."
                        ),
                        Receita(
                                farm = boaVista,
                                tipo = TipoReceita.PRESTACAO_SERVICO,
                                descricao = "Prestacao de servico de suporte sanitario e quarentena",
                                valor = BigDecimal("4200.00"),
                                data = hoje.minusDays(2),
                                dataVencimento = hoje.plusDays(12),
                                formaPagamento = FormaPagamento.TRANSFERENCIA,
                                status = StatusLancamentoFinanceiro.PENDENTE,
                                responsavel = admin,
                                comprador = "Fazenda Parceira Vale Verde",
                                observacoes = "Receita recorrente de apoio operacional entre propriedades."
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

        alertaRepository.saveAll(
                listOf(
                        Alerta(
                                farm = santaHelena,
                                tipo = TipoAlerta.RECOMENDACAO_VENDA,
                                prioridade = PrioridadeAlerta.MEDIA,
                                titulo = "Falcao acima da curva media de engorda",
                                mensagem = "O animal Falcao vem sustentando GMD acima da media do lote nas ultimas semanas.",
                                animal = animal9,
                                recomendacao = "Usar o animal como referencia para estrategia nutricional do lote.",
                                status = StatusAlerta.ATIVO
                        ),
                        Alerta(
                                farm = boaVista,
                                tipo = TipoAlerta.SEM_PESAGEM_RECENTE,
                                prioridade = PrioridadeAlerta.MEDIA,
                                titulo = "Atualizar peso da novilha Gaia",
                                mensagem = "A novilha Gaia precisa de nova pesagem para manter a curva produtiva atualizada.",
                                animal = animal12,
                                recomendacao = "Agendar passagem no curral na proxima rodada operacional.",
                                status = StatusAlerta.ATIVO
                        )
                )
        )

        val assinatura = assinaturaRepository.save(
                Assinatura(
                        empresa = empresa,
                        tipo = TipoAssinatura.PREMIUM,
                        status = StatusAssinatura.ATIVA,
                        periodoPagamento = PeriodoPagamento.MENSAL,
                        dataInicio = agora.minusMonths(2),
                        dataVencimento = agora.plusDays(18),
                        proximaCobranca = agora.plusDays(18),
                        valor = BigDecimal("349.00"),
                        asaasSubscriptionId = "sub_demo_premium_001"
                )
        )

        pagamentoRepository.saveAll(
                listOf(
                        Pagamento(
                                assinatura = assinatura,
                                valor = BigDecimal("349.00"),
                                dataVencimento = agora.minusDays(42),
                                dataPagamento = agora.minusDays(40),
                                status = StatusPagamento.PAGO,
                                metodoPagamento = MetodoPagamento.PIX,
                                transacaoId = "pay_demo_001_pago",
                                gatewayProvider = "asaas-demo",
                                asaasSubscriptionId = "sub_demo_premium_001",
                                invoiceUrl = "https://demo.bovcore.local/faturas/001",
                                pixPayload = "00020126580014BR.GOV.BCB.PIX0114bovcore-demo520400005303986540679.005802BR5925BOVCORE DEMO AGRO6009SAO PAULO62070503***6304ABCD"
                        ),
                        Pagamento(
                                assinatura = assinatura,
                                valor = BigDecimal("349.00"),
                                dataVencimento = agora.minusDays(12),
                                dataPagamento = agora.minusDays(10),
                                status = StatusPagamento.PAGO,
                                metodoPagamento = MetodoPagamento.PIX,
                                transacaoId = "pay_demo_002_pago",
                                gatewayProvider = "asaas-demo",
                                asaasSubscriptionId = "sub_demo_premium_001",
                                invoiceUrl = "https://demo.bovcore.local/faturas/002",
                                pixPayload = "00020126580014BR.GOV.BCB.PIX0114bovcore-demo520400005303986540679.005802BR5925BOVCORE DEMO AGRO6009SAO PAULO62070503***6304EFGH"
                        )
                )
        )

        logger.info("Seed showroom BovCore criado com sucesso.")
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
                                tipo = TipoAssinatura.PREMIUM,
                                status = StatusAssinatura.ATIVA,
                                periodoPagamento = PeriodoPagamento.MENSAL,
                                dataInicio = agora.minusMonths(2),
                                dataVencimento = agora.plusDays(18),
                                proximaCobranca = agora.plusDays(18),
                                valor = BigDecimal("349.00"),
                                asaasSubscriptionId = "sub_demo_premium_001"
                        )
                )

        assinatura.tipo = TipoAssinatura.PREMIUM
        assinatura.status = StatusAssinatura.ATIVA
        assinatura.periodoPagamento = PeriodoPagamento.MENSAL
        assinatura.dataVencimento = agora.plusDays(18)
        assinatura.proximaCobranca = agora.plusDays(18)
        assinatura.valor = BigDecimal("349.00")
        assinatura.asaasSubscriptionId = assinatura.asaasSubscriptionId ?: "sub_demo_premium_001"
        assinaturaRepository.save(assinatura)

        val pagamentosExistentes = pagamentoRepository.findByAssinaturaEmpresaIdOrderByDataVencimentoDesc(empresa.id!!)
        if (pagamentosExistentes.isNotEmpty()) {
            pagamentoRepository.deleteAll(pagamentosExistentes)
        }

        pagamentoRepository.saveAll(
                listOf(
                        Pagamento(
                                assinatura = assinatura,
                                valor = BigDecimal("349.00"),
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
                                valor = BigDecimal("349.00"),
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
