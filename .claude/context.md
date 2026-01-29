# Contexto do Projeto iBoi

## Visão Geral
Plataforma SaaS de gestão de gado para produtores rurais com foco em simplicidade, controle operacional e evolução com IA.

## Stack Tecnológica
- **Backend**: Kotlin + Spring Boot
- **Segurança**: Spring Security + JWT
- **Banco de Dados**: PostgreSQL + Flyway
- **Arquitetura**: Monólito com Hexagonal (Ports & Adapters)
- **Frontend**: React
- **Infraestrutura futura**: Docker + Cloud

## Fases do Projeto

### Fase 1 – MVP Operacional (ATUAL)
- [ ] Autenticação e usuários
- [ ] Cadastro de propriedades (Farms)
- [ ] Cadastro de animais
- [ ] Eventos básicos (vacinas, movimentações)
- [ ] API REST documentada

### Fase 2 – Gestão Avançada
- Controle sanitário completo
- Histórico do animal
- Gestão financeira
- Relatórios operacionais

### Fase 3 – Inteligência e Escala
- IA para alertas
- Predição de doenças
- Integrações externas
- Dashboards inteligentes

## Módulos do Sistema

### 1. Identity (Autenticação e Autorização)
**Status**: Em implementação - Refatoração concluída parcialmente

**Estrutura Hexagonal**:
- `api/` - Controllers e DTOs (camada de apresentação)
- `application/` - Use Cases e Services (camada de aplicação)
- `domain/` - Entidades e regras de negócio
- `infrastructure/` - Repositories (camada de persistência)

**Conceitos Principais**:
- **Usuario**: Usuário do sistema
- **Profile**: Perfil básico do usuário
- **Farm**: Propriedade/Fazenda (tenant do sistema)
- **UserFarmProfile**: Relacionamento usuário-fazenda com perfil
- **Role/RoleEnum**: Papéis (OWNER, ADMIN, WORKER, etc.)
- **FarmRole**: Papel específico em uma fazenda
- **Permission/RolePermission**: Permissões granulares
- **FarmModule**: Módulos habilitados por fazenda (planos)

**Use Cases Implementados**:
- `AuthenticateUserUseCase` - Login
- `OnboardingUseCase` - Cadastro inicial + primeira fazenda
- `CreateProfileUseCase` - Criar perfil
- `GenerateFarmTokenUseCase` - Gerar token com contexto de fazenda
- `ListUserFarmsUseCase` - Listar fazendas do usuário
- `ResolvePermissionsUseCase` - Resolver permissões do usuário
- `AssignUserToFarmUseCase` - Atribuir usuário a fazenda

**Endpoints**:
- `POST /auth/login` - Login
- `POST /auth/register` - Registro básico
- `POST /auth/onboarding` - Onboarding completo
- `GET /profile/farms` - Listar fazendas do usuário
- `POST /onboarding` - Processo de onboarding

### 2. Rebanho
**Status**: Não rastreado no git (em desenvolvimento)
- Cadastro de animais
- Eventos básicos
- Movimentações

### 3. Sanitário
**Status**: Não rastreado no git (planejado)
- Vacinas
- Tratamentos
- Calendário sanitário

### 4. Financeiro
**Status**: Não rastreado no git (planejado)
- Custos e despesas
- Receitas
- Fluxo de caixa

### 5. Plano/Assinatura
**Status**: Parcialmente implementado
- Modelos: `Assinatura`, `Pagamento`, `PlanoPreco`, `PeriodoPagamento`, `StatusAssinatura`
- Controle de acesso por plano
- Filter para validar assinatura ativa

### 6. IA
**Status**: Não rastreado no git (fase 3)

### 7. Relatório
**Status**: Não rastreado no git (fase 2)

## Arquitetura Multi-Tenant

O sistema usa **multi-tenancy baseado em Farm (fazenda)**:

1. **FarmContext**: Armazena o farm_id atual da requisição
2. **TenantContext**: Contexto de tenant (legacy, sendo substituído)
3. **JwtAuthFilter**: Extrai farm_id do token JWT
4. **AssinaturaFilter**: Valida se a fazenda tem assinatura ativa

**Fluxo de Autenticação**:
```
1. Usuario faz login → recebe JWT básico
2. Usuario seleciona uma Farm → recebe JWT com farm_id
3. Requisições incluem farm_id no token
4. Filters extraem farm_id e validam permissões/assinatura
5. Queries filtram automaticamente por farm_id
```

## Modelo de Negócio
- SaaS com assinatura mensal
- Planos por quantidade de animais
- Funcionalidades premium (IA, relatórios avançados)

## Decisões Arquiteturais

### 1. Por que Hexagonal?
- Separação clara de responsabilidades
- Facilita testes
- Independência de frameworks
- Preparação para evolução/microserviços futuros

### 2. Multi-Tenancy por Farm
- Cada produtor pode ter múltiplas fazendas
- Usuários podem ter diferentes permissões por fazenda
- Isolamento de dados por fazenda
- Facilita modelo de cobrança

### 3. Migração de auth → identity
- Sistema antigo muito acoplado
- Novo sistema com conceitos mais claros (Farm, Profile, Role)
- Melhor suporte a multi-tenancy
- Preparação para SSO e integrações futuras

## Estado Atual da Implementação

### ✅ BACKEND - Implementado
- ✅ Estrutura hexagonal completa do módulo Identity
- ✅ Entidades de domínio (Usuario, Farm, Profile, Role, FarmRole, etc.)
- ✅ Use cases implementados:
  - AuthenticateUserUseCase (login com retorno de farms)
  - OnboardingUseCase (cadastro completo + trial 30 dias)
  - CreateProfileUseCase
  - GenerateFarmTokenUseCase
  - ListUserFarmsUseCase
  - ResolvePermissionsUseCase
  - AssignUserToFarmUseCase
- ✅ Repositories (Usuario, Empresa, Farm, UserFarmProfile, etc.)
- ✅ JwtService para geração de tokens
- ✅ SecurityConfig com JWT + AssinaturaFilter
- ✅ Controllers:
  - AuthController (POST /auth/login)
  - OnboardingController (POST /onboarding)
  - ProfileController (GET /profile/farms)
- ✅ Sistema de assinatura TRIAL (30 dias automáticos)
- ✅ Multi-tenancy baseado em Farm
- ✅ CORS configurado para frontend

### ✅ FRONTEND - Implementado
- ✅ React 19 + TypeScript + Vite
- ✅ TailwindCSS para estilização
- ✅ React Router DOM para rotas
- ✅ Axios configurado com interceptors (JWT automático)
- ✅ Tela de Login completa e funcional
- ✅ Tela de Onboarding (wizard 3 etapas):
  - Etapa 1: Dados pessoais (nome, email, telefone, senha)
  - Etapa 2: Empresa (nome, CNPJ, tipo)
  - Etapa 3: Fazenda (nome, cidade, estado, tipo produção, tamanho)
- ✅ Tipos TypeScript (OnboardingRequest, LoginRequest, LoginResponse, etc.)
- ✅ Páginas criadas (Dashboard, Animais, Eventos, Veterinários, etc.)
- ✅ Layout base e navegação

### ⚠️ PROBLEMAS IDENTIFICADOS
1. **Backend LoginResponse vs Frontend**:
   - Backend retorna `farms` e `defaultFarmId`
   - Frontend espera `usuario` no LoginResponse
   - Precisa ajustar um dos dois para compatibilidade

2. **Arquivos não rastreados no git**:
   - Módulos rebanho/, financeiro/, sanitario/, relatorio/, ia/
   - Alguns arquivos de identity/ marcados como "A" (added)
   - Precisa revisar antes de commit

3. **Testes**: Nenhum teste implementado ainda

### 🚧 Em Progresso / Próximos Passos
- [ ] Corrigir incompatibilidade LoginResponse
- [ ] Testar fluxo completo Login + Onboarding
- [ ] Implementar módulo Rebanho (CORE DO MVP)
- [ ] Finalizar páginas do Dashboard
- [ ] Implementar seletor de Farm (trocar de fazenda)
- [ ] Refresh token (opcional para MVP)

### ❌ Pendente (Fase 1 - MVP)
- [ ] Módulo Rebanho completo:
  - Cadastro de animais
  - Eventos básicos (vacina, movimentação)
  - Listagem e filtros
- [ ] Módulo Sanitário básico
- [ ] Módulo Financeiro básico
- [ ] Relatórios básicos
- [ ] Documentação OpenAPI completa
- [ ] Testes unitários e integração
- [ ] Deploy (Docker + Cloud)

## Próximos Passos Sugeridos

1. **Finalizar módulo Identity**
   - Testar endpoints de auth/onboarding
   - Validar fluxo completo de autenticação
   - Implementar refresh token (se necessário)

2. **Implementar módulo Rebanho (CORE do MVP)**
   - Cadastro de animais
   - Eventos básicos
   - Movimentações entre lotes/piquetes

3. **Frontend básico**
   - Telas de login/onboarding
   - Dashboard inicial
   - Cadastro de animais

4. **Deploy MVP**
   - Configurar Docker
   - Deploy em cloud
   - Configurar CI/CD básico

## Comandos Úteis

```bash
# Rodar aplicação
./gradlew bootRun

# Rodar testes
./gradlew test

# Build
./gradlew build

# Migrations
./gradlew flywayMigrate
```

## Notas Importantes

- O sistema está em refatoração ativa (muitos arquivos deletados do módulo `auth`)
- Não commitar até validar que tudo funciona
- Manter padrão hexagonal em todos os módulos
- Frontend está na pasta `frontend/`
- Arquivos não rastreados precisam ser revisados antes de commit

---

**Última atualização**: 2026-01-29
**Fase atual**: MVP - Fase 1
**Foco atual**: Finalizar módulo Identity
