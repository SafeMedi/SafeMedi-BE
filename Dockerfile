FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --chown=spring:spring app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]