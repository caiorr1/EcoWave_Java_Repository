# Use uma imagem base leve e de alto desempenho
FROM openjdk:8-jdk-alpine

# Defina um diretório de trabalho padrão
WORKDIR /app

# Crie um usuário não privilegiado
RUN addgroup -S appuser && adduser -S -G appuser appuser

# Defina ARG e ENV
ARG JAR_FILE=target/ecowave-0.0.1-SNAPSHOT.jar
ENV APP_HOME /app

# Copie o arquivo JAR do build local para o container
COPY ${JAR_FILE} ${APP_HOME}/ecowave.jar

# Mude a propriedade do diretório para o usuário não privilegiado
RUN chown -R appuser:appuser ${APP_HOME}

# Execute o container como o usuário não privilegiado
USER appuser

# Exponha a porta que o Spring Boot vai usar
EXPOSE 8081

# Defina o comando de inicialização para rodar o app em background
ENTRYPOINT ["java","-jar","/app/ecowave.jar"]
