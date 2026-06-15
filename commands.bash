#!/usr/bin/env bash

# Arquivo de referência para estudos.
# Rode os blocos separadamente em terminais diferentes quando quiser observar os logs.

# 1. RabbitMQ local com painel de gerenciamento.
# Painel: http://localhost:15672
# Login: guest
# Senha: guest
docker run -d --name rabbitmq-local \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

# 2. Banco do email-service.
cd /Users/level33/lab/base-api/user-mail-sender-ms/email
docker compose up -d

# 3. Banco do user-service.
cd /Users/level33/lab/base-api/user-mail-sender-ms/user
docker compose up -d

# 4. Rodar email-service.
cd /Users/level33/lab/base-api/user-mail-sender-ms/email
EMAIL_USERNAME=local@test.com \
EMAIL_PASSWORD=local \
SPRING_RABBITMQ_ADDRESSES=amqp://localhost:5672 \
SPRING_RABBITMQ_USERNAME=guest \
SPRING_RABBITMQ_PASSWORD=guest \
SPRING_RABBITMQ_VIRTUAL_HOST=/ \
mvn spring-boot:run

# 5. Rodar user-service em outro terminal.
cd /Users/level33/lab/base-api/user-mail-sender-ms/user
SPRING_RABBITMQ_ADDRESSES=amqp://localhost:5672 \
SPRING_RABBITMQ_USERNAME=guest \
SPRING_RABBITMQ_PASSWORD=guest \
SPRING_RABBITMQ_VIRTUAL_HOST=/ \
./mvnw spring-boot:run

# 6. Criar um usuário.
curl -X POST http://localhost:8081/api/v1/users/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Joao Silva",
    "email": "joao.silva@email.com"
  }'

# 7. Criar três usuários de uma vez e disparar a fila simulated-delay-queue.
curl -X POST http://localhost:8081/api/v1/users/create/batch \
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

# 8. Listar usuários e disparar a fila users-list-queue.
curl http://localhost:8081/api/v1/users/list
