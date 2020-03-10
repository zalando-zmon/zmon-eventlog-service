ZMON source code on GitHub is no longer in active development. Zalando will no longer actively review issues or merge pull-requests.

ZMON is still being used at Zalando and serves us well for many purposes. We are now deeper into our observability journey and understand better that we need other telemetry sources and tools to elevate our understanding of the systems we operate. We support the `OpenTelemetry <https://opentelemetry.io>`_ initiative and recommended others starting their journey to begin there.

If members of the community are interested in continuing developing ZMON, consider forking it. Please review the licence before you do.

=====================
ZMON EventLog Service
=====================

.. image:: https://img.shields.io/badge/OpenTracing-enabled-blue.svg
    :target: http://opentracing.io
    :alt: OpenTracing enabled


Create database schema:

.. code-block:: bash

    docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres
    psql -h localhost -U postgres -c 'CREATE DATABASE local_eventlog_db;'
    psql -h localhost -U postgres -d local_eventlog_db -f database/eventlog/00_create_schema.sql


Build docker image:

.. code-block:: bash

    $ ./mvnw clean install
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
