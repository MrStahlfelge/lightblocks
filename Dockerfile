FROM eclipse-temurin:8
RUN adduser --system --group --home /app app
USER app
WORKDIR /app
COPY --chown=app:app server/build/libs/server.jar .
EXPOSE 8887
CMD ["java", "-jar", "/app/server.jar"]
