FROM zalando/openjdk:8u40-b09-2

RUN mkdir /app

ADD target/zmon-eventlog-service-1.0-SNAPSHOT.jar /app/zmon-eventlog-service.jar

WORKDIR /app

ENV SERVER_PORT 8081

EXPOSE 8081

CMD ["java","-jar","zmon-eventlog-service.jar"]
