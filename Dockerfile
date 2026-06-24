# Etapa 1: Compilación
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copiar archivos de configuración de Maven y el código fuente
COPY pom.xml .
COPY src ./src
# Compilar y generar el archivo .jar omitiendo los tests
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución ejecutable
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiar el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
# Exponer el puerto por defecto de Spring Boot
EXPOSE 8080
# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]