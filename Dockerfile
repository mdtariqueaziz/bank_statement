FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/bank-statement.jar bank-statement.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","bank-statement.jar"]