# Use Java 17
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy project
COPY . .

# Build project
RUN ./mvnw clean package -DskipTests

# Run app
CMD ["java", "-jar", "target/kanban-0.0.1-SNAPSHOT.jar"]
