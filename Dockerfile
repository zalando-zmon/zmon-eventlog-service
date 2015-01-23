FROM dockerfile/java:oracle-java7

RUN mkdir /app

ADD target/zmon-eventlog-service-1.0-SNAPSHOT.jar /app/zmon-eventlog-service.jar

WORKDIR /app

ENV SERVER_PORT 38084

EXPOSE 38084

CMD ["java","-jar","zmon-eventlog-service.jar"]
