# Sprint 0: Dominio Canonico BovCore

## Objetivo

Definir uma linguagem unica entre backend, frontend, mobile e produto para evitar retrabalho durante a Fase 1.

## Regra principal

Os nomes canonicos de contrato devem usar ASCII e portugues de negocio.

Isso significa:

- Sem acentos em chaves JSON.
- Sem misturar nomes em ingles e portugues no mesmo contrato de API.
- Classes internas podem continuar em ingles quando isso ja estiver consolidado na infraestrutura, desde que o contrato externo fique padronizado.

## Entidades canonicas

### Fazenda

- Nome canonico de negocio: `fazenda`
- Entidade tecnica atual: `Farm`
- Contrato externo preferencial: `fazenda` e campos em portugues quando o fluxo for de cadastro

### Pasto

- Nome canonico de negocio: `pasto`
- Entidade tecnica atual: `Pasture`
- Contrato externo preferencial: `pasto`

### Animal

- Nome canonico de negocio: `animal`
- Identificadores canonicos: `brinco`, `rfid`, `codigoSisbov`

### Producao

- Nome canonico de contrato: `tipoProducao`
- Alias legado aceito temporariamente: `tipoProdução`

## Convencoes de contrato

### Requests de entrada

- Preferir nomes em portugues de negocio.
- Preferir chaves ASCII.
- Aceitar alias legados apenas quando necessario para nao quebrar compatibilidade.

### Responses de saida

- Manter compatibilidade com o que ja esta publicado.
- Quando houver evolucao, privilegiar consistencia por modulo.

## Decisoes aplicadas nesta sprint

- Onboarding passa a tratar `tipoProducao` como chave canonica.
- Backend continua aceitando o alias legado `tipoProdução` durante a transicao.
- Proximas tasks da Sprint 0 devem seguir esta mesma regra ao tocar contratos.

## Proximos alinhamentos obrigatorios

- Consolidar o vocabulario de `fazenda` versus `farm`.
- Consolidar o vocabulario de `pasto` versus `pasture`.
- Definir contrato canonico da ficha `Animal 360`.
- Definir nomenclatura unica para eventos, vacinacoes e movimentacoes.
