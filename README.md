# Estudos sobre Microserviços e Mensageria com RabbitMQ

## 1. Visão geral

Este documento apresenta os principais conceitos de **microserviços** e **mensageria**, usando como base um projeto com dois serviços independentes:

* `user`: responsável pelo cadastro e gerenciamento de usuários.
* `email`: responsável por consumir mensagens relacionadas ao envio de e-mails.

O objetivo inicial do projeto é entender como dois serviços podem se comunicar de forma **assíncrona** usando o **RabbitMQ** como broker de mensagens.

Fluxo simplificado:

```text
user -> RabbitMQ -> email
```

Neste cenário, o serviço `user` não chama diretamente o serviço `email`.
Em vez disso, ele publica uma mensagem em uma fila, e o serviço `email` consome essa mensagem quando estiver disponível.

---

## 2. O que são microserviços?

Microserviços são uma abordagem arquitetural em que uma aplicação é dividida em serviços menores, independentes e especializados.

Cada serviço deve possuir uma responsabilidade bem definida dentro do sistema.

Em vez de uma única aplicação monolítica cuidar de autenticação, usuários, pagamentos, e-mails, relatórios e demais regras de negócio, o sistema é separado em partes menores.

Exemplo:

```text
Sistema monolítico:
Uma única aplicação faz tudo.

Sistema com microserviços:
Serviço de usuários
Serviço de e-mails
Serviço de pagamentos
Serviço de notificações
Serviço de relatórios
```

No projeto atual, temos:

```text
user-service
email-service
```

O `user-service` cuida da criação de usuários.

O `email-service` cuida do processamento de mensagens relacionadas a e-mail.

---

## 3. Por que usar microserviços?

A principal ideia dos microserviços é permitir que cada parte do sistema evolua de forma mais independente.

Algumas vantagens:

* Separação clara de responsabilidades.
* Maior desacoplamento entre partes do sistema.
* Possibilidade de escalar serviços separadamente.
* Facilidade para times diferentes trabalharem em serviços diferentes.
* Menor impacto ao alterar uma regra interna de um serviço.

Por exemplo, o serviço `user` não precisa saber como o serviço `email` envia e-mails internamente.

Ele apenas informa que um usuário foi criado.

Quem decide o que fazer com essa informação é o serviço `email`.

---

## 4. O que é mensageria?

Mensageria é uma forma de comunicação entre sistemas por meio de mensagens.

Em vez de um serviço chamar outro diretamente via HTTP, ele envia uma mensagem para um intermediário chamado **broker**.

O broker recebe, armazena e entrega essa mensagem para quem estiver interessado nela.

No projeto, o broker utilizado é o **RabbitMQ**.

Fluxo conceitual:

```text
Producer -> Broker -> Consumer
```

No projeto:

```text
user -> RabbitMQ -> email
```

---

## 5. Comunicação síncrona vs assíncrona

### Comunicação síncrona

Na comunicação síncrona, um serviço chama outro diretamente e espera uma resposta.

Exemplo:

```text
user-service -> HTTP -> email-service
```

Nesse modelo, se o `email-service` estiver fora do ar, lento ou com erro, o `user-service` pode ser impactado.

Exemplo de fluxo síncrono:

```text
1. Cliente cria usuário.
2. user-service salva o usuário.
3. user-service chama email-service via HTTP.
4. user-service espera o email-service responder.
5. A requisição termina.
```

### Comunicação assíncrona

Na comunicação assíncrona, um serviço publica uma mensagem e não precisa esperar o processamento final.

Exemplo:

```text
user-service -> RabbitMQ -> email-service
```

Nesse modelo, o `user-service` apenas publica uma mensagem informando que algo aconteceu.

O `email-service` consome essa mensagem depois.

Exemplo de fluxo assíncrono:

```text
1. Cliente cria usuário.
2. user-service salva o usuário.
3. user-service publica uma mensagem no RabbitMQ.
4. A requisição termina.
5. email-service consome a mensagem.
6. email-service processa a mensagem.
```

Esse modelo reduz o acoplamento entre os serviços.

---

## 6. Principais conceitos de mensageria

## 6.1 Producer

O **producer** é quem produz e envia uma mensagem.

No projeto, o producer está no serviço `user`.

Quando um usuário é criado, o serviço `user` pode publicar uma mensagem na fila.

Exemplo conceitual de mensagem:

```text
Usuário criado: João Silva
```

Exemplo em Java usando `RabbitTemplate`:

```java
rabbitTemplate.convertAndSend("", "email-queue", "Usuário criado: João Silva");
```

Nesse caso, o serviço `user` está publicando uma mensagem na fila `email-queue`.

---

## 6.2 Consumer

O **consumer** é quem consome a mensagem.

No projeto, o consumer está no serviço `email`.

Ele fica escutando a fila e executa alguma ação quando uma nova mensagem chega.

Neste momento do estudo, a ação esperada é apenas imprimir a mensagem no terminal.

Exemplo:

```java
@RabbitListener(queues = "email-queue")
public void listenEmailQueue(String text) {
    System.out.println(text);
}
```

Quando uma mensagem chega na fila `email-queue`, o Spring executa automaticamente o método anotado com `@RabbitListener`.

---

## 6.3 Broker

O **broker** é o intermediário responsável por receber, armazenar e entregar mensagens.

Neste projeto, o broker é o **RabbitMQ**.

Ele fica entre o producer e o consumer.

Fluxo:

```text
Producer -> RabbitMQ -> Consumer
```

No projeto:

```text
user-service -> RabbitMQ -> email-service
```

O serviço `user` publica mensagens no RabbitMQ.

O serviço `email` consome mensagens do RabbitMQ.

---

## 6.4 Queue

A **queue**, ou fila, é onde as mensagens ficam armazenadas até serem consumidas.

No projeto, a fila usada é:

```text
email-queue
```

O producer precisa publicar nessa fila.

O consumer precisa escutar essa mesma fila.

Exemplo no producer:

```java
rabbitTemplate.convertAndSend("", "email-queue", "Mensagem de teste");
```

Exemplo no consumer:

```java
@RabbitListener(queues = "email-queue")
public void listenEmailQueue(String text) {
    System.out.println(text);
}
```

Se o producer publicar em uma fila e o consumer escutar outra, a mensagem não será consumida.

---

## 6.5 Exchange

A **exchange** é o componente do RabbitMQ que recebe mensagens do producer e decide para qual fila elas devem ser roteadas.

No RabbitMQ, o producer normalmente não publica diretamente na fila.
Ele publica em uma exchange, e a exchange encaminha a mensagem para uma ou mais filas.

Porém, existe uma exchange especial chamada **default exchange**.

Quando usamos:

```java
rabbitTemplate.convertAndSend("", "email-queue", "Mensagem de teste");
```

O primeiro parâmetro vazio:

```java
""
```

representa a **exchange padrão** do RabbitMQ.

Com a exchange padrão, a `routing key` precisa ter o mesmo nome da fila.

Ou seja:

```text
exchange = ""
routingKey = "email-queue"
queue = "email-queue"
```

Fluxo:

```text
Producer
   |
   v
Default Exchange
   |
   v
email-queue
   |
   v
Consumer
```

---

## 6.6 Routing Key

A **routing key** é a chave usada para direcionar a mensagem.

Quando usamos a exchange padrão, a routing key deve ser exatamente o nome da fila.

Neste projeto:

```text
routingKey = email-queue
```

Exemplo:

```java
rabbitTemplate.convertAndSend("", "email-queue", "Mensagem simples");
```

Aqui, o RabbitMQ entende que a mensagem deve ser entregue para a fila chamada `email-queue`.

---

## 7. Fluxo atual do projeto

O fluxo esperado neste momento é:

```text
1. Cliente faz POST /users
2. UserController recebe os dados da requisição
3. UserService executa a regra de criação do usuário
4. UserService salva o usuário
5. UserProducer publica uma mensagem na fila
6. RabbitMQ armazena a mensagem na email-queue
7. EmailConsumer escuta a email-queue
8. EmailConsumer imprime a mensagem no terminal
```

Representação visual:

```text
POST /users
    |
    v
UserController
    |
    v
UserService
    |
    v
UserProducer
    |
    v
RabbitMQ
    |
    v
email-queue
    |
    v
EmailConsumer
    |
    v
System.out.println(...)
```

Esse fluxo prova que existe comunicação assíncrona entre os dois serviços.

---

## 8. Criando um usuário pelo terminal

Com o serviço `user` rodando na porta `8081`, é possível criar um usuário usando `curl`.

Exemplo:

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Joao Silva",
    "email": "joao@email.com"
  }'
```

Esse comando envia uma requisição HTTP para o serviço `user`.

Se o fluxo estiver configurado corretamente:

```text
1. O usuário será criado.
2. O user-service publicará uma mensagem no RabbitMQ.
3. O email-service consumirá a mensagem.
4. A mensagem será exibida no terminal do email-service.
```

Para validar o fluxo, os seguintes componentes precisam estar rodando:

```text
RabbitMQ
user-service
email-service
```

---

## 9. Configurações importantes

## 9.1 Os serviços precisam usar o mesmo RabbitMQ

O `user-service` e o `email-service` precisam apontar para o mesmo broker.

Se o `user-service` publicar em um RabbitMQ e o `email-service` escutar outro RabbitMQ, a mensagem nunca será consumida.

Verifique principalmente:

```properties
spring.rabbitmq.addresses
spring.rabbitmq.username
spring.rabbitmq.password
spring.rabbitmq.virtual-host
```

Exemplo:

```properties
spring.rabbitmq.addresses=amqp://localhost:5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/
```

---

## 9.2 Os serviços precisam usar a mesma fila

O nome da fila precisa ser igual nos dois lados.

Producer:

```java
rabbitTemplate.convertAndSend("", "email-queue", "Mensagem simples");
```

Consumer:

```java
@RabbitListener(queues = "email-queue")
public void listenEmailQueue(String text) {
    System.out.println(text);
}
```

Se o producer enviar para:

```text
email-queue
```

mas o consumer escutar:

```text
outra-fila
```

a mensagem não será consumida pelo consumer esperado.

---

## 9.3 A fila precisa existir

Para que o fluxo funcione corretamente, a fila precisa existir no RabbitMQ.

Ela pode ser criada manualmente no painel do RabbitMQ ou declarada pelo próprio Spring.

Exemplo declarando a fila no Spring:

```java
@Bean
public Queue emailQueue() {
    return new Queue("email-queue", true);
}
```

O segundo parâmetro `true` indica que a fila será durável.

```java
new Queue("email-queue", true);
```

Isso significa que a fila continua existindo mesmo se o RabbitMQ for reiniciado.

---

## 10. Enviando String simples vs DTO

Neste momento do estudo, o objetivo é enviar uma mensagem simples em formato de texto.

Exemplo:

```text
Usuário criado: João Silva
```

Esse formato é suficiente para entender o fluxo básico de mensageria.

Porém, em um cenário real, o consumer normalmente precisa de mais informações estruturadas.

Nesse caso, podemos enviar um DTO em JSON.

Exemplo:

```json
{
  "userId": "uuid-do-usuario",
  "emailTo": "joao@email.com",
  "emailSubject": "Bem-vindo",
  "body": "Olá João, seja bem-vindo ao sistema."
}
```

Comparação:

| Formato    | Quando usar                                       |
| ---------- | ------------------------------------------------- |
| `String`   | Bom para testes iniciais e entendimento do fluxo  |
| `DTO/JSON` | Melhor para cenários reais com dados estruturados |

---

## 11. Conversor de mensagem

Quando enviamos apenas texto simples, o conversor padrão do Spring costuma ser suficiente.

Exemplo no producer:

```java
rabbitTemplate.convertAndSend("", "email-queue", "Mensagem simples");
```

Exemplo no consumer:

```java
@RabbitListener(queues = "email-queue")
public void listenEmailQueue(String text) {
    System.out.println(text);
}
```

Quando o projeto evoluir para envio de objetos Java, como um `EmailDto`, é comum configurar um conversor JSON.

Exemplo:

```java
@Bean
public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

Esse conversor transforma objetos Java em JSON ao enviar e transforma JSON em objetos Java ao consumir.

---

## 12. Exemplo de estrutura conceitual

Uma estrutura possível para o serviço `user`:

```text
user-service
 └── src/main/java
     └── com.example.user
         ├── controller
         │   └── UserController.java
         ├── service
         │   └── UserService.java
         ├── producer
         │   └── UserProducer.java
         └── config
             └── RabbitMQConfig.java
```

Uma estrutura possível para o serviço `email`:

```text
email-service
 └── src/main/java
     └── com.example.email
         ├── consumer
         │   └── EmailConsumer.java
         └── config
             └── RabbitMQConfig.java
```

---

## 13. Responsabilidade de cada camada

## 13.1 UserController

O `UserController` recebe a requisição HTTP.

Exemplo de responsabilidade:

```text
Receber POST /users
Validar entrada
Chamar UserService
Retornar resposta HTTP
```

Ele não deveria conter regra complexa de negócio.

---

## 13.2 UserService

O `UserService` executa a regra de negócio.

Exemplo de responsabilidade:

```text
Criar usuário
Salvar usuário
Chamar UserProducer para publicar evento
```

Ele coordena o fluxo da criação do usuário.

---

## 13.3 UserProducer

O `UserProducer` é responsável por publicar mensagens no RabbitMQ.

Exemplo:

```java
@Component
public class UserProducer {

    private final RabbitTemplate rabbitTemplate;

    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserCreatedMessage(String message) {
        rabbitTemplate.convertAndSend("", "email-queue", message);
    }
}
```

Responsabilidade:

```text
Enviar mensagem para o broker
```

---

## 13.4 EmailConsumer

O `EmailConsumer` é responsável por consumir mensagens do RabbitMQ.

Exemplo:

```java
@Component
public class EmailConsumer {

    @RabbitListener(queues = "email-queue")
    public void listenEmailQueue(String text) {
        System.out.println("Mensagem recebida: " + text);
    }
}
```

Responsabilidade:

```text
Escutar a fila
Receber mensagens
Executar uma ação com base na mensagem recebida
```

Neste momento, a ação é apenas imprimir no terminal.

---

## 14. Ponto importante: evento não é comando direto

Quando o `user-service` publica uma mensagem como:

```text
Usuário criado: João Silva
```

ele está informando que algo aconteceu.

Isso é um evento.

Um evento representa um fato do passado.

Exemplos:

```text
UserCreated
OrderPaid
PaymentFailed
EmailRequested
```

O `user-service` não deve depender de como o `email-service` vai tratar esse evento.

A ideia é:

```text
O user-service informa o acontecimento.
O email-service decide o que fazer com essa informação.
```

Isso aumenta o desacoplamento entre os serviços.

---

## 15. Cuidados importantes em sistemas com mensageria

## 15.1 O consumer pode estar fora do ar

Se o `email-service` estiver parado, o `user-service` ainda pode publicar mensagens no RabbitMQ.

As mensagens ficam na fila até o consumer voltar.

Esse é um dos benefícios da comunicação assíncrona.

---

## 15.2 A mensagem pode ser processada mais de uma vez

Em sistemas com mensageria, é possível que uma mensagem seja entregue ou processada mais de uma vez.

Por isso, em sistemas reais, o consumer deve ser pensado para lidar com repetição.

Esse conceito é chamado de **idempotência**.

Exemplo:

```text
Se a mesma mensagem de envio de e-mail chegar duas vezes, o sistema deve evitar enviar dois e-mails iguais sem necessidade.
```

---

## 15.3 Podem acontecer erros no consumo

O consumer pode falhar ao processar uma mensagem.

Exemplos:

```text
Erro ao enviar e-mail
Erro de conexão com banco
Erro de integração externa
Mensagem inválida
```

Por isso, em uma evolução do projeto, é importante estudar:

```text
Retry
Dead Letter Queue
Logs
Monitoramento
Idempotência
```

---

## 16. Evolução natural do projeto

Uma boa ordem de evolução para este estudo é:

```text
1. Fazer o user-service enviar uma String simples.
2. Fazer o email-service imprimir essa String no terminal.
3. Criar um EmailDto.
4. Publicar o EmailDto como JSON.
5. Consumir o EmailDto no email-service.
6. Transformar o DTO em uma solicitação real de envio de e-mail.
7. Implementar EmailService.
8. Enviar e-mail real.
9. Salvar status do envio no banco.
10. Tratar erros de consumo.
11. Implementar retry.
12. Implementar Dead Letter Queue.
13. Adicionar logs estruturados.
14. Adicionar rastreabilidade/correlationId.
```

---

## 17. Resumo mental

Neste projeto:

```text
user-service = producer
RabbitMQ = broker
email-queue = queue
email-service = consumer
```

Fluxo principal:

```text
user-service publica uma mensagem
RabbitMQ armazena a mensagem
email-service consome a mensagem
email-service imprime a mensagem no terminal
```

Resumo simplificado:

```text
POST /users
   ↓
user-service cria o usuário
   ↓
user-service publica mensagem
   ↓
RabbitMQ recebe na email-queue
   ↓
email-service consome
   ↓
Mensagem aparece no terminal
```

Objetivo atual:

```text
Provar que a comunicação assíncrona entre os serviços está funcionando.
```

Depois disso, o projeto pode evoluir para envio de DTO, envio real de e-mail, tratamento de erro, retry e Dead Letter Queue.

---

## 18. Checklist para validar se está funcionando

Antes de testar, confirme:

* RabbitMQ está rodando.
* `user-service` está rodando.
* `email-service` está rodando.
* Os dois serviços apontam para o mesmo RabbitMQ.
* Os dois serviços usam o mesmo virtual host.
* O producer publica na fila `email-queue`.
* O consumer escuta a fila `email-queue`.
* A fila existe no RabbitMQ.
* O terminal do `email-service` está aberto para visualizar o `System.out.println`.

Teste com:

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Joao Silva",
    "email": "joao@email.com"
  }'
```

Resultado esperado no terminal do `email-service`:

```text
Mensagem recebida: Usuário criado: João Silva
```

Se essa mensagem aparecer, o fluxo básico de mensageria está funcionando.

```
```
