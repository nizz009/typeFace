# Stage 1: Build the application
FROM amazoncorretto:17-alpine AS build

WORKDIR /app
COPY gradlew .
COPY gradle gradle/
COPY . .

RUN chmod +x gradlew
RUN ./gradlew build -x test

# Stage 2: Runtime image
FROM amazoncorretto:17-alpine

WORKDIR /app
COPY --from=build /app/build/libs/ByteBridge-0.0.1-SNAPSHOT.jar .

CMD ["java", "-jar", "ByteBridge-0.0.1-SNAPSHOT.jar"]