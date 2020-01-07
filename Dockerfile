FROM registry.opensource.zalan.do/stups/openjdk:latest

COPY target/zmon-eventlog-service-1.0-SNAPSHOT.jar /zmon-eventlog-service.jar

ENV SERVER_PORT 8081

EXPOSE 8081

CMD java $JAVA_OPTS $(java-dynamic-memory-opts) -jar /zmon-eventlog-service.jar
