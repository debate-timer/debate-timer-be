FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR /workspace

ADD https://dtdg.co/latest-java-tracer ./dd-java-agent.jar

COPY --from=builder /workspace/dependencies/ ./
COPY --from=builder /workspace/spring-boot-loader/ ./
COPY --from=builder /workspace/snapshot-dependencies/ ./
COPY --from=builder /workspace/application/ ./

ENTRYPOINT ["java", "-javaagent:./dd-java-agent.jar", "org.springframework.boot.loader.launch.JarLauncher"]
