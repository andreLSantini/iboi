# Seed Demo Producao

Esta seed cria um ambiente de demonstracao comercial do BovCore, com dados completos para apresentar produto em producao.

## Como ativar

Defina a variavel de ambiente:

```bash
APP_SEED_ENABLED=true
```

Ao subir a aplicacao com essa flag, o sistema cria a base demo automaticamente na primeira execucao.

## Credenciais demo

- Usuario master: `demo@bovcore.com.br`
- Senha: `bovcore123`

- Usuario operacao: `operacao@bovcore.com.br`
- Senha: `bovcore123`

## O que a seed entrega

- Empresa demo completa
- Duas fazendas com dados operacionais e fundiarios
- Pastos e lotes com narrativa de manejo
- Rebanho com genealogia, recria, engorda, matrizes e quarentena
- Eventos produtivos, sanitarios e reprodutivos
- Vacinacoes com proximas doses
- Movimentacoes entre pastos, lotes e entrada/saida externa
- Despesas e receitas para fluxo de caixa e demonstracao financeira
- Alertas de IA e operacao
- Assinatura `PREMIUM` ativa para liberar a vitrine completa

## Observacoes

- A seed e idempotente para o usuario demo principal: se ele ja existir, o sistema nao recria toda a base.
- Nesse caso, o runner apenas reconcilia o estado de cobranca da assinatura demo.
- Para recriar tudo do zero, limpe os dados do ambiente antes de subir novamente com a flag.
