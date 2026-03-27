# Deploy de Producao

## Estado atual

- Banco e migracoes: `Flyway`
- Banco recomendado: `PostgreSQL`
- Billing: `Asaas` por `PIX` e `boleto`
- Stack publicada: `frontend + backend + postgres` via `Docker Compose`

## Onde subir

Para este repositório, o caminho mais simples e estável hoje é:

- `Hetzner`, `DigitalOcean`, `Vultr` ou outra VPS Linux
- `Docker` + `Docker Compose`
- domínio apontando para a VPS

Esse caminho é o mais direto porque:

- o frontend já faz proxy para o backend no mesmo domínio
- o webhook do Asaas já consegue bater em `/webhooks/asaas`
- o PostgreSQL pode subir junto no compose

## Variáveis de ambiente

Crie um `.env` a partir de `.env.example`.

Valores mínimos:

- `DATABASE_NAME`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `ASAAS_ENABLED=true`
- `ASAAS_BASE_URL=https://api.asaas.com` em produção real
- `ASAAS_API_KEY`
- `ASAAS_WEBHOOK_TOKEN`

## Subida

```bash
docker compose --env-file .env up -d --build
```

## Endpoints públicos importantes

- App: `/`
- API: `/api/...`
- Login: `/auth/...`
- Onboarding: `/onboarding`
- Webhook Asaas: `/webhooks/asaas`

## Webhook Asaas

No painel do Asaas:

- URL: `https://SEU-DOMINIO/webhooks/asaas`
- Header/token: `asaas-access-token`
- Valor: o mesmo de `ASAAS_WEBHOOK_TOKEN`

## Observações

- Hoje o projeto usa `spring.flyway.enabled=true` em produção.
- O `ddl-auto` ainda está em `update` como rede de segurança para o beta.
- Antes de escalar comercialmente, o ideal é evoluir isso para migracoes 100% fechadas e `ddl-auto=validate`.
