# Estudos sobre Microservicos e Mensageria

## 1. Microservicos

Microservicos sao servicos menores, cada um com uma responsabilidade bem definida.

Em vez de uma unica aplicacao cuidar de tudo, o sistema e dividido em partes independentes. Cada servico pode ter seu proprio banco, suas proprias regras e sua propria forma de evoluir.

Neste projeto existem dois servicos principais:

- `user`: responsavel por criar e gerenciar usuarios.
- `email`: responsavel por consumir mensagens relacionadas a email.

A ideia principal e manter os servicos desacoplados. O servico de usuario nao precisa saber como o servico de email funciona internamente. Ele apenas publica uma mensagem dizendo que algo aconteceu.

## 2. Mensageria

Mensageria e uma forma de comunicacao entre sistemas usando mensagens.

Em vez de um servico chamar diretamente o outro por HTTP, ele envia uma mensagem para um intermediario chamado **broker**. O outro servico consome essa mensagem quando estiver disponivel.

No projeto, o broker usado e o **RabbitMQ**.

Fluxo geral:

```text
user -> RabbitMQ -> email
```

Isso ajuda a desacoplar os servicos. O `user` nao precisa esperar o `email` terminar o trabalho. Ele apenas envia a mensagem para a fila.

## 3. Conceitos importantes

### Producer

Producer e quem produz/envia uma mensagem.

No projeto, o producer esta no servico `user`.

Quando um usuario e criado, o servico `user` pode publicar uma mensagem na fila.

Exemplo conceitual:

```text
Usuario criado: Joao
```

### Consumer

Consumer e quem consome/recebe uma mensagem.

No projeto, o consumer esta no servico `email`.

Ele fica escutando a fila e, quando chega uma mensagem, executa algum comportamento.

Neste momento do estudo, o comportamento esperado e apenas imprimir a mensagem no terminal.

### Broker

Broker e o intermediario entre producer e consumer.

Neste projeto, o broker e o RabbitMQ.

O producer envia a mensagem para o RabbitMQ, e o consumer recebe a mensagem a partir dele.

### Queue

Queue, ou fila, e onde as mensagens ficam armazenadas ate serem consumidas.

No projeto, a fila usada e:

```text
email-queue
```

O servico `user` precisa publicar nessa fila, e o servico `email` precisa escutar essa mesma fila.

### Exchange

Exchange e o componente do RabbitMQ que recebe mensagens do producer e decide para qual fila elas devem ir.

No projeto, ao usar:

```java
rabbitTemplate.convertAndSend("", routingKey, message);
```

o primeiro parametro vazio (`""`) representa a **exchange padrao** do RabbitMQ.

Quando usamos a exchange padrao, a `routingKey` deve ser o nome da fila.

Exemplo:

```java
rabbitTemplate.convertAndSend("", "email-queue", "Mensagem de teste");
```

Nesse caso, a mensagem e enviada diretamente para a fila `email-queue`.

### Routing Key

Routing key e a chave usada para direcionar uma mensagem.

Com a exchange padrao, a routing key precisa ter o mesmo nome da fila.

Neste projeto:

```text
routingKey = email-queue
```

## 4. Fluxo atual do projeto

O fluxo esperado neste momento e:

```text
1. Cliente faz POST /users
2. UserController recebe os dados
3. UserService salva o usuario
4. UserProducer publica uma mensagem simples na fila
5. RabbitMQ armazena a mensagem na fila email-queue
6. EmailConsumer escuta a fila
7. EmailConsumer imprime a mensagem no terminal
```

Visualmente:

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
RabbitMQ: email-queue
    |
    v
EmailConsumer
    |
    v
System.out.println(...)
```

## 5. Criando um usuario pelo terminal

Com o servico `user` rodando na porta `8081`, e possivel criar um usuario usando `curl`:

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Joao Silva",
    "email": "joao@email.com"
  }'
```

Esse comando envia uma requisicao HTTP para o servico `user`.

Se o fluxo estiver configurado corretamente, ao criar o usuario o servico `user` tambem publica uma mensagem na fila.

Para verificar a chegada da mensagem, o servico `email` precisa estar rodando em outro terminal.

## 6. Pontos importantes para funcionar

### Os dois servicos precisam usar o mesmo RabbitMQ

O `user` e o `email` precisam apontar para o mesmo broker.

Se o `user` publica em um RabbitMQ e o `email` escuta outro RabbitMQ, a mensagem nunca vai chegar.

Verificar principalmente:

- `spring.rabbitmq.addresses`
- `spring.rabbitmq.username`
- `spring.rabbitmq.password`
- `spring.rabbitmq.virtual-host`

### Os dois servicos precisam usar a mesma fila

O nome da fila precisa ser igual nos dois lados.

Producer:

```text
email-queue
```

Consumer:

```java
@RabbitListener(queues = "email-queue")
```

Se o producer enviar para `email-queue`, mas o consumer escutar `outra-fila`, nada sera consumido.

### String simples vs DTO

Neste momento do estudo, o objetivo e enviar uma string simples.

Exemplo:

```text
Usuario criado: Joao Silva
```

Mais para frente, o projeto pode evoluir para enviar um DTO com mais informacoes.

Exemplo:

```json
{
  "userId": "uuid-do-usuario",
  "emailTo": "joao@email.com",
  "emailSubject": "Welcome",
  "body": "Hello Joao"
}
```

A diferenca principal:

- `String`: bom para entender o fluxo basico.
- `DTO/JSON`: melhor quando o consumer precisa de varios dados estruturados.

### Conversor de mensagem

Quando o projeto envia objetos Java como DTO, normalmente se usa um conversor JSON, como `Jackson2JsonMessageConverter`.

Quando o projeto envia apenas texto simples, o conversor padrao ja costuma ser suficiente.

Por isso, se a ideia for estudar string simples, o foco deve ser:

```java
rabbitTemplate.convertAndSend("", routingKey, "Mensagem simples");
```

E no consumer:

```java
public void listenEmailQueue(String text) {
    System.out.println(text);
}
```

## 7. Papel do EmailConsumer

O consumer de email trabalha como um listener.

Ele nao precisa enviar nada. Ele apenas fica escutando a fila.

Exemplo conceitual:

```java
@RabbitListener(queues = "email-queue")
public void listenEmailQueue(String text) {
    System.out.println(text);
}
```

Quando uma mensagem chega na fila `email-queue`, o metodo anotado com `@RabbitListener` e executado automaticamente pelo Spring.

## 8. Evolucao natural do projeto

Uma boa ordem de estudo para este projeto:

1. Fazer o `user` enviar uma string simples.
2. Fazer o `email` imprimir essa string no terminal.
3. Trocar a string por um `EmailDto`.
4. Fazer o `email` converter o DTO recebido em um modelo de email.
5. Enviar email real usando `EmailService`.
6. Salvar o status do envio no banco.
7. Tratar erros de consumo.
8. Estudar retry, dead letter queue e logs.

## 9. Resumo mental

O `user` e o producer.

O RabbitMQ e o broker.

A `email-queue` e a fila.

O `email` e o consumer.

O producer publica mensagens.

O consumer escuta mensagens.

Neste momento, o objetivo e apenas provar que a comunicacao funciona:

```text
user publica -> RabbitMQ entrega -> email imprime no terminal
```
