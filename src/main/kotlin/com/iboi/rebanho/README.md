# Módulo Rebanho - iBoi

## Visão Geral

O módulo Rebanho é responsável pela gestão completa do rebanho bovino, incluindo:
- Cadastro e controle de animais
- Organização em lotes
- Registro de eventos (vacinação, pesagem, movimentação, etc.)

## Arquitetura Hexagonal

Segue o padrão Hexagonal (Ports & Adapters):

```
rebanho/
├── api/                  # Camada de apresentação (Controllers, DTOs)
│   ├── AnimalController.kt
│   ├── EventoController.kt
│   ├── LoteController.kt
│   ├── dto/
│   │   ├── AnimalDto.kt
│   │   ├── EventoDto.kt
│   │   └── LoteDto.kt
│   └── exception/
│       └── GlobalExceptionHandler.kt
├── application/          # Camada de aplicação (Use Cases)
│   └── usecase/
│       ├── CadastrarAnimalUseCase.kt
│       ├── ListarAnimaisUseCase.kt
│       ├── BuscarAnimalPorIdUseCase.kt
│       ├── AtualizarAnimalUseCase.kt
│       ├── DeletarAnimalUseCase.kt
│       ├── RegistrarEventoUseCase.kt
│       ├── CadastrarLoteUseCase.kt
│       ├── ListarLotesUseCase.kt
│       ├── BuscarLotePorIdUseCase.kt
│       ├── AtualizarLoteUseCase.kt
│       └── DeletarLoteUseCase.kt
├── domain/              # Camada de domínio (Entidades, Enums)
│   ├── Animal.kt
│   ├── Evento.kt
│   ├── Lote.kt
│   ├── CategoriaAnimal.kt
│   ├── Sexo.kt
│   ├── StatusAnimal.kt
│   ├── Raca.kt
│   └── TipoEvento.kt
└── repository/          # Camada de infraestrutura (Repositories)
    ├── AnimalRepository.kt
    ├── EventoRepository.kt
    └── LoteRepository.kt
```

## Endpoints da API

### Animais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/animais` | Cadastrar novo animal |
| GET | `/api/animais` | Listar animais (com filtros e paginação) |
| GET | `/api/animais/{id}` | Buscar animal por ID |
| PUT | `/api/animais/{id}` | Atualizar animal |
| DELETE | `/api/animais/{id}` | Deletar animal (soft delete) |

**Filtros disponíveis:**
- `status`: ATIVO, VENDIDO, MORTO, DESCARTADO, TRANSFERIDO
- `categoria`: BEZERRO, NOVILHO, NOVILHA, BOI, VACA, TOURO, MATRIZ
- `loteId`: UUID do lote
- `sexo`: MACHO, FEMEA
- `page`: Número da página (padrão: 0)
- `size`: Tamanho da página (padrão: 20)

**Exemplo de request:**
```json
POST /api/animais
{
  "brinco": "001",
  "nome": "Estrela",
  "sexo": "FEMEA",
  "raca": "NELORE",
  "dataNascimento": "2023-01-15",
  "pesoAtual": 150.5,
  "categoria": "BEZERRO",
  "loteId": "uuid-do-lote",
  "observacoes": "Animal saudável"
}
```

### Lotes

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/lotes` | Cadastrar novo lote |
| GET | `/api/lotes` | Listar lotes (com paginação) |
| GET | `/api/lotes/{id}` | Buscar lote por ID |
| PUT | `/api/lotes/{id}` | Atualizar lote |
| DELETE | `/api/lotes/{id}` | Deletar lote (apenas se vazio) |

**Filtros disponíveis:**
- `apenasAtivos`: true/false
- `page`: Número da página
- `size`: Tamanho da página

**Exemplo de request:**
```json
POST /api/lotes
{
  "nome": "Lote 1 - Engorda",
  "descricao": "Lote para engorda de novilhos"
}
```

### Eventos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/eventos` | Registrar novo evento |
| GET | `/api/eventos` | Listar eventos (com filtros) |
| GET | `/api/eventos/animal/{animalId}` | Histórico de um animal |
| GET | `/api/eventos/{id}` | Buscar evento por ID |

**Tipos de eventos:**
- VACINA, VERMIFUGO, PESAGEM, MOVIMENTACAO, NASCIMENTO, DESMAME
- MORTE, VENDA, COMPRA, TRATAMENTO, INSEMINACAO, COBERTURA
- PARTO, DIAGNOSTICO_GESTACAO, DESCARTE, OBSERVACAO

**Exemplo de request:**
```json
POST /api/eventos
{
  "animalId": "uuid-do-animal",
  "tipo": "PESAGEM",
  "data": "2024-01-15",
  "descricao": "Pesagem mensal",
  "peso": 180.5
}
```

## Regras de Negócio

### Multi-tenancy
- Todos os dados são isolados por Farm (fazenda)
- O `farmId` é extraído do token JWT
- Validação automática em todos os endpoints

### Unicidade de Brinco
- Cada animal deve ter brinco único dentro da fazenda
- Validação na criação e atualização

### Soft Delete de Animais
- Animais não são deletados fisicamente
- Status alterado para `DESCARTADO`
- Mantém histórico completo

### Eventos Especiais

**Evento de Pesagem:**
- Atualiza automaticamente `animal.pesoAtual`
- Mantém histórico de todas as pesagens

**Evento de Movimentação:**
- Atualiza automaticamente `animal.lote`
- Registra lote de origem e destino

### Proteção de Lote
- Não é possível deletar lote com animais
- Erro 400 com mensagem clara

### Validações

Todos os DTOs possuem Bean Validation:
- `@NotBlank`, `@NotNull` para campos obrigatórios
- `@Size(max=X)` para limitar tamanho de strings
- `@DecimalMin`, `@DecimalMax` para valores numéricos
- `@Past`, `@PastOrPresent` para datas

## Exception Handling

O módulo possui tratamento global de exceções:

| Exceção | Status HTTP | Descrição |
|---------|-------------|-----------|
| `AnimalNaoEncontradoException` | 404 | Animal não encontrado |
| `BrincoDuplicadoException` | 409 | Brinco já existe |
| `LoteNaoEncontradoException` | 404 | Lote não encontrado |
| `DadosInvalidosException` | 400 | Dados inválidos |
| `AcessoNegadoException` | 403 | Acesso negado |
| `MethodArgumentNotValidException` | 400 | Erro de validação |

**Exemplo de resposta de erro:**
```json
{
  "message": "Já existe um animal com este brinco",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Frontend React

O frontend está localizado em `/frontend` e inclui:

### Páginas
- **Animais** (`/animais`) - Listagem com filtros e paginação
- **Detalhes do Animal** (`/animais/:id`) - Histórico completo
- **Eventos** (`/eventos`) - Registro e listagem de eventos
- **Lotes** (`/lotes`) - Gestão completa de lotes

### Funcionalidades
- Grid de cards responsivo
- Busca em tempo real
- Filtros múltiplos
- Paginação client e server-side
- Modais de criação/edição
- Validação de formulários
- Tratamento de erros amigável
- Confirmação de exclusão

### Serviços
- `animalService.ts` - API de animais
- `eventoService.ts` - API de eventos
- `loteService.ts` - API de lotes

## Como Executar

### Backend
```bash
# Rodar aplicação
./gradlew bootRun

# Rodar testes
./gradlew test

# Build
./gradlew build
```

### Frontend
```bash
cd frontend

# Instalar dependências
npm install

# Rodar em desenvolvimento
npm run dev

# Build para produção
npm run build
```

## Testes

Para rodar os testes do módulo Rebanho:
```bash
./gradlew test --tests "com.iboi.rebanho.*"
```

## Documentação Swagger

Acesse a documentação completa em:
```
http://localhost:8080/swagger-ui.html
```

Todos os endpoints estão documentados com:
- Descrição completa
- Exemplos de request/response
- Códigos de status HTTP
- Parâmetros e validações

## Performance

### Otimizações Implementadas
1. **Query otimizada com filtros no banco**
   - Evita carregar todos os dados em memória
   - Filtros aplicados diretamente no SQL

2. **Paginação em todos os endpoints de listagem**
   - Reduz payload da resposta
   - Melhora tempo de resposta

3. **Índices no banco de dados**
   - `brinco + farm_id` (único)
   - `status + farm_id`
   - `lote_id`

## Próximos Passos

### Melhorias Sugeridas
- [ ] Implementar cache com Redis
- [ ] Adicionar webhooks para eventos críticos
- [ ] Exportação de relatórios em PDF/Excel
- [ ] Integração com balanças IoT
- [ ] Dashboard analytics com gráficos
- [ ] App mobile nativo

### Integrações Futuras
- [ ] Módulo Sanitário (calendário de vacinas)
- [ ] Módulo Financeiro (custos por animal)
- [ ] Módulo IA (predição de doenças)
- [ ] API pública para integrações

## Suporte

Para dúvidas ou problemas:
- Documentação: `/docs`
- Issues: GitHub Issues
- Email: suporte@iboi.com.br

---

**Versão:** 1.0.0
**Última atualização:** 2026-01-29
**Status:** ✅ Produção Ready
