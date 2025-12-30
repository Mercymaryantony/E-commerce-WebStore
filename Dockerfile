# Stage 1: Build the application
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Fix build.gradle issues before building
RUN sed -i 's/maxHeapSize = "4g"/\/\/ maxHeapSize = "4g"  # Commented out for Docker build compatibility/' build.gradle || true
# Comment out the custom buildDir line - using a more flexible pattern
RUN sed -i 's|^buildDir = file.*|// buildDir override commented out for Docker build|' build.gradle || true

# Create gradle.properties to override buildDir
RUN echo "org.gradle.project.buildDir=build" > gradle.properties

# Build the application (skip tests, checkstyle, and PMD for faster build)
# Checkstyle and PMD are already run in CI pipeline separately
RUN gradle clean build -x test -x checkstyleMain -x checkstyleTest -x pmdMain -x pmdTest --no-daemon

# Verify JAR exists and copy to expected location if needed
RUN if [ ! -f build/libs/*.jar ]; then \
        JAR_FILE=$(find ~/.gradle-builds -name "*.jar" -type f 2>/dev/null | head -1); \
        if [ -n "$JAR_FILE" ]; then \
            mkdir -p build/libs && \
            cp "$JAR_FILE" build/libs/; \
        fi; \
    fi

# Stage 2: Create runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application with prod profile
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]