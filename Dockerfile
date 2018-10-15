FROM openjdk:11-jre-slim

RUN mkdir /app

WORKDIR /app

ADD ./api/target/users-api-1.0.0-SNAPSHOT.jar /app

EXPOSE 8081

CMD java -jar users-api-1.0.0-SNAPSHOT.jar
