FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY src ./src
COPY web ./web
RUN javac -d out src/*.java

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/out ./src
COPY --from=build /app/web ./web
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-cp", "src", "HelpDeskServer"]
