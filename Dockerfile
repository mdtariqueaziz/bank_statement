FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY ./target/bank-statement.jar /app

EXPOSE 8081

ENTRYPOINT ["java","-jar","bank-statement.jar"]