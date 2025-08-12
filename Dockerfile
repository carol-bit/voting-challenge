# ---- Build stage ----
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copia apenas os poms primeiro para cache das dependências
COPY pom.xml .
# Se tiver módulos, copie os poms desses módulos também
# COPY module-a/pom.xml module-a/pom.xml
# COPY module-b/pom.xml module-b/pom.xml

RUN mvn -q -e -U -DskipTests dependency:go-offline

# Agora copia o restante do código
COPY src ./src
# (Se houver módulos, copie-os aqui)
# COPY module-a ./module-a
# COPY module-b ./module-b

RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Ajuste o nome do arquivo se seu artifactId diferir
#ARG APP_JAR=target/*-SNAPSHOT.jar
#COPY --from=build /workspace/${APP_JAR} app.jar
COPY --from=build /workspace/target/*.jar app.jar


# Opções padrão (memória, logs, etc.)
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=default

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
