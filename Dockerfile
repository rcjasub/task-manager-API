FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw package -DskipTests
EXPOSE 8081
CMD ["java", "-jar", "target/taskmanager-0.0.1-SNAPSHOT.jar"]
