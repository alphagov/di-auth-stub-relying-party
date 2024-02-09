FROM gradle:7.6.4-jdk17@sha256:e9e95840061ff9c1c28f5c3cf8c5bc1295f71e3f88bbc250826d599e9df04c09 AS build
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
