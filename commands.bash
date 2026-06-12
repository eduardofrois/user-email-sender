cd /Users/level33/lab/base-api/user-mail-sender-ms/email

EMAIL_USERNAME=local@test.com \
EMAIL_PASSWORD=local \
RABBITMQ_USERNAME=guest \
RABBITMQ_PASSWORD=guest \
SPRING_RABBITMQ_ADDRESSES=amqp://localhost:5672 \
SPRING_RABBITMQ_VIRTUAL_HOST=/ \
mvn spring-boot:run


==============================================================


cd /Users/level33/lab/base-api/user-mail-sender-ms/user

EMAIL_QUEUE=email-queue \
RABBITMQ_USERNAME=guest \
RABBITMQ_PASSWORD=guest \
SPRING_RABBITMQ_ADDRESSES=amqp://localhost:5672 \
SPRING_RABBITMQ_VIRTUAL_HOST=/ \
./mvnw spring-boot:run