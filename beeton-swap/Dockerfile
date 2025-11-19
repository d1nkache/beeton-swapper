FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package


FROM tomcat:10-jre17
COPY --from=build /app/target/beeton-swap-api.war /usr/local/tomcat/webapps/beeton-swap-api.war
