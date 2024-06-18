# DB Migrator Service

## About The Project
This project is a centralized microservice designed to store registered bank account information in a legacy database 
of ABC bank with poor performance. It controls the load on the server by ensuring the query rate does not exceed 2 
queries per second.

## Design
The DB migrator consumes RabbitMQ messages collected from identity services to store them in the legacy database of 
ABC Bank, ensuring the database is not overloaded.

## Runbook

### Running the application stand-alone

```bash
mvn clean verify
mvn spring-boot:run
```

### Dockerizing the application

Please follow the instructions of the README file that is located in the root folder of the project to run all dockerized microservices.

## RabbitMQ

This application consumes the IdentityExchange exchange on RabbitMQ, which is published by identity services.
Following XML format used to pass the information of registered bank accounts from identity services to DB migrator service.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<consumeExport>
    <accountNumber>053</accountNumber>
    <iban>NL31ABNC1000000000</iban>
    <name>Keano van Dongen</name>
    <address>3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG</address>
    <dob>2020-10-10</dob>
    <documentNr>vbkpjcnchg6p</documentNr>
</consumeExport>
```