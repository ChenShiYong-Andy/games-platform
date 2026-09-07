ARG OPENJDK_IMAGE=mcr.microsoft.com/openjdk/jdk:21-ubuntu

FROM ${OPENJDK_IMAGE} AS builder
ENV APP_HOME=/app
WORKDIR ${APP_HOME}
COPY target/games-platform-*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract

FROM ${OPENJDK_IMAGE}
ENV APP_HOME=/app
ENV SPRING_PROFILES_ACTIVE=docker

WORKDIR ${APP_HOME}
COPY --from=builder ${APP_HOME}/application/application.jar ./application.jar
COPY --from=builder ${APP_HOME}/application/lib/ ./lib/

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Duser.timezone=GMT+08 -Djava.awt.headless=true -jar application.jar \"$@\"", "--"]
