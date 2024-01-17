FROM gradle:7.6.3-jdk17@sha256:f9f1354cc82ebb02be6699e5840f899f203bc6a0906998bce470f63ea113b4b5 AS build
WORKDIR /home/gradle/src

COPY --chown=gradle:gradle gradlew build.gradle ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle tools tools
RUN gradle clean build --no-daemon

COPY --chown=gradle:gradle src src
RUN gradle clean build installDist --no-daemon

FROM amazoncorretto:17.0.8-alpine3.17 as runtime
COPY --from=build /home/gradle/src/build/install/src .

ENTRYPOINT ["bin/src"]
