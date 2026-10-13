# Stage 1: Build the JAR with JDK 25 and Maven Wrapper
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

# Copy Maven wrapper configuration and dependencies descriptor
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Normalize line endings and grant execution permission
RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

# Download dependencies for layer caching
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build the JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Lightweight runtime image with JRE 25
FROM eclipse-temurin:25-jre
WORKDIR /app

# Ensure logs directory exists for Logback rolling file appender
RUN mkdir -p /app/logs

# Copy the compiled JAR artifact from the builder stage
COPY --from=builder /build/target/excelsior-*.jar app.jar

# Expose the default port
EXPOSE 8080

# Launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]
