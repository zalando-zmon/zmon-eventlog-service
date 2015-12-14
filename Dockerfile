FROM registry.opensource.zalan.do/stups/openjdk:8u66-b17-1-8

RUN mkdir /app

ADD target/zmon-eventlog-service-1.0-SNAPSHOT.jar /app/zmon-eventlog-service.jar

WORKDIR /app

ENV SERVER_PORT 8081

EXPOSE 8081

CMD ["java","-jar","zmon-eventlog-service.jar"]
