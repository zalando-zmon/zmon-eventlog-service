FROM registry.opensource.zalan.do/stups/openjdk:8u66-b17-1-8

COPY target/zmon-eventlog-service-1.0-SNAPSHOT.jar /zmon-eventlog-service.jar
COPY target/scm-source.json /

ENV SERVER_PORT 8081

EXPOSE 8081

CMD java $JAVA_OPTS $(java-dynamic-memory-opts) -jar /zmon-eventlog-service.jar
