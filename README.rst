=====================
ZMON EventLog Service
=====================

Create database schema:

.. code-block:: bash

    docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres
    psql -h localhost -U postgres -d local_eventlog_db -f database/eventlog/00_create_schema.sql


Build docker image:

.. code-block:: bash

    $ ./mvnw clean install
    $ scm-source -f target/scm-source.json
    $ docker build -t zmon-eventlog-service .

Run with PostgreSQL:

.. code-block:: bash

    java -jar target/zmon-eventlog-service-1.0-SNAPSHOT.jar

Create Event:

.. code-block:: bash

    curl -X POST http://localhost:8080/events \
         -d "[{\"typeId\":212993, \"time\":\"2014-01-01T20:00:00.000\",\"attributes\":{\"alertId\":1,\"entity\":\"elsn01:5827\"}}]" \
         -H "Content-Type: application/json"


Read Event:

.. code-block:: bash

    curl 'http://localhost:8080/events?types=212993&key=alertId&value=1&from=0'
