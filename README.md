# Estudos sobre Microserviços e Mensageria com RabbitMQ

Este repositório é um projeto de estudos. Ele não tenta ser um serviço real de produção. A ideia é praticar Spring Boot, PostgreSQL, RabbitMQ, filas, producers, consumers e comunicação assíncrona entre dois serviços.

## Visão Geral

O projeto tem dois serviços:

- `user`: expõe uma API REST para CRUD de usuários.
- `email`: consome mensagens do RabbitMQ e simula ações relacionadas a e-mail.

Fluxo principal:

```text
client -> user-service -> RabbitMQ -> email-service
```

O `user-service` roda na porta `8081`.
O `email-service` roda na porta `8080`.
O RabbitMQ local é acessado em `localhost:5672`.

## Estrutura Atual

```text
user/
  src/main/java/dev/java10x/user
    controller/UserController.java
    service/UserService.java
    producer/UserProducer.java
    mapper/UserMapper.java
    mapper/ProducerMapper.java
    dto/CreateUserRequest.java
    dto/UpdateUserRequest.java
    dto/PatchUserRequest.java
    dto/UserResponse.java
    dto/ProducerDto.java
    enums/EventType.java
    exception/GlobalExceptionHandler.java

email/
  src/main/java/dev/java10x/email
    consumer/EmailConsumer.java
    service/EmailService.java
    helpers/Delay.java
    configuration/RabbitMq.java
```

## Bancos Locais

Cada serviço tem seu próprio PostgreSQL via Docker Compose.

User database:

```text
localhost:5435 -> container:5432
database: ms-user-ms
user: postgres-user
password: postgres-password
```

Email database:

```text
localhost:5433 -> container:5432
database: ms_email_ms
user: postgres
password: postgres
```

Subir os bancos:

```bash
cd user
docker compose up -d
```

```bash
cd email
docker compose up -d
```

## RabbitMQ Local

Os dois serviços apontam para um RabbitMQ local:

```yaml
spring:
  rabbitmq:
    addresses: amqp://localhost:5672
    username: guest
    password: guest
    virtual-host: /
```

Filas usadas no estudo:

- `email-queue`: recebe evento quando um usuário é criado.
- `simulated-delay-queue`: recebe evento quando usuários são criados em lote.

As filas são declaradas no `email-service`, em `RabbitMq.java`.

## Eventos

O `user-service` usa `ProducerDto` para montar os eventos enviados para o RabbitMQ.

Campos do evento:

```json
{
  "userId": "uuid",
  "name": "Nome do usuario",
  "email": "email@teste.com",
  "eventType": "USER_CREATED"
}
```

Tipos enviados atualmente pelo `UserProducer`:

- `USER_CREATED`
- `SIMULATED_DELAY_REQUESTED`

Neste projeto, o producer converte o DTO para JSON string antes de enviar. Isso mantém o consumer simples para estudo, recebendo `String` com `@Payload`.

## Rotas do User Service

Base URL:

```text
http://localhost:8081/api/v1/users
```

### Criar usuário

Criar um usuário:

```bash
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Joao Silva",
    "email": "joao.silva@email.com"
  }'
```

O fluxo esperado:

```text
POST /api/v1/users
  -> salva usuário no banco do user-service
  -> publica evento USER_CREATED na email-queue
  -> email-service consome e imprime a mensagem
```

### Criar usuários em lote

Criar três usuários de uma vez:

```bash
curl -X POST http://localhost:8081/api/v1/users/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "name": "Joao Silva",
      "email": "joao.silva@email.com"
    },
    {
      "name": "Maria Souza",
      "email": "maria.souza@email.com"
    },
    {
      "name": "Carlos Lima",
      "email": "carlos.lima@email.com"
    }
  ]'
```

O fluxo esperado:

```text
POST /api/v1/users/batch
  -> salva os usuários no banco do user-service
  -> publica um evento por usuário na simulated-delay-queue
  -> email-service consome cada mensagem
  -> EmailService.simulateEmailSending imprime e executa delay de 5 segundos
```

### Listar usuários

Listar usuários:

```bash
curl http://localhost:8081/api/v1/users
```

O fluxo esperado:

```text
GET /api/v1/users
  -> busca usuários no banco
  -> retorna a lista na resposta HTTP
```

### Buscar usuário por ID

```bash
curl http://localhost:8081/api/v1/users/11111111-1111-1111-1111-111111111111
```

O fluxo esperado:

```text
GET /api/v1/users/{userId}
  -> busca usuário por UUID
  -> retorna 200 com o usuário encontrado
  -> retorna 404 se o usuário não existir
```

### Atualizar usuário

Atualização completa com `PUT`:

```bash
curl -X PUT http://localhost:8081/api/v1/users/11111111-1111-1111-1111-111111111111 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Joao Silva Atualizado",
    "email": "joao.atualizado@email.com"
  }'
```

O fluxo esperado:

```text
PUT /api/v1/users/{userId}
  -> exige name e email
  -> atualiza o usuário existente
  -> retorna 200 com os dados atualizados
  -> retorna 404 se o usuário não existir
```

### Atualizar parcialmente

Atualização parcial com `PATCH`:

```bash
curl -X PATCH http://localhost:8081/api/v1/users/11111111-1111-1111-1111-111111111111 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Joao Parcial"
  }'
```

O fluxo esperado:

```text
PATCH /api/v1/users/{userId}
  -> atualiza somente os campos enviados
  -> exige ao menos um campo no corpo da requisição
  -> retorna 200 com os dados atualizados
  -> retorna 404 se o usuário não existir
```

### Deletar usuário

```bash
curl -X DELETE http://localhost:8081/api/v1/users/11111111-1111-1111-1111-111111111111
```

O fluxo esperado:

```text
DELETE /api/v1/users/{userId}
  -> remove o usuário existente
  -> retorna 204 sem corpo
  -> retorna 404 se o usuário não existir
```

### Respostas de erro

O `user-service` retorna respostas padronizadas para erros comuns:

```json
{
  "timestamp": "2026-06-16T23:00:00-03:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuário não encontrado para o código informado: uuid",
  "path": "/api/v1/users/uuid",
  "fields": null
}
```

Validações de campos retornam `400 Bad Request` com o mapa `fields`.

## Rodando os Serviços

Use o arquivo `commands.bash` como referência dos comandos atuais.

Ordem recomendada:

1. Subir RabbitMQ local.
2. Subir o PostgreSQL do `email`.
3. Subir o PostgreSQL do `user`.
4. Rodar `email-service`.
5. Rodar `user-service`.
6. Testar as rotas com `curl`.

Exemplo de RabbitMQ local:

```bash
docker run -d --name rabbitmq-local \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

Painel do RabbitMQ:

```text
http://localhost:15672
login: guest
senha: guest
```

## Pontos de Estudo

Este projeto é bom para estudar:

- diferença entre HTTP síncrono e mensageria assíncrona
- producer e consumer com RabbitMQ
- filas declaradas por Spring
- DTOs para payload de eventos
- DTOs separados para entrada e saída HTTP
- mapper entre entidade e DTO
- tratamento global de erros com `@RestControllerAdvice`
- serialização JSON
- transações com `@Transactional`
- uso de `save` e `saveAll`
- delay simulado para observar processamento assíncrono
- separação entre API, service, producer, consumer e repository

## Limitações Intencionais

Por ser um projeto de estudo, algumas decisões são simplificadas:

- não há autenticação
- não há retry ou dead letter queue
- os consumers imprimem mensagens no terminal
- os eventos são enviados por filas simples usando a default exchange
- o delay é artificial e serve apenas para observar comportamento no terminal

Esses pontos são bons próximos passos de estudo quando o fluxo básico estiver claro.
