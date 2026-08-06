FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} lirium.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/lirium.jar"]