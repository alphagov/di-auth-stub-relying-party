FROM gradle:7.6.4-jdk17@sha256:50233d3e173694d1d941523836db9051365d87bd69b53ef1c5567e5f98574c52 AS build
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
