# Identity Service

## About The Project
This project is an internal scalable authentication service that distributes user credential data across multiple 
databases, alleviating the load on ABC Bank's legacy database, which previously suffered from performance issues, 
thereby enhancing customer service quality.

## Design
Identity service is responsible for storing bank account credentials and authenticating users, using bank account 
information gathered from the API gateway which was routed with hash code of username. It also generates mandatory 
fields for newly registered accounts, such as the default password, account number, and IBAN. Each identity service 
uses a predefined "bankAccount.branchCode" configuration as a suffix for the account number. To ensure the uniqueness 
of account numbers, the identity service increments the stored latest account number by one in database. Identity service 
is also responsible with passing the account information such as accountNumber, iban, name, address, dob, documentNr to
DB migrator service with rabbitMQ message.

## Runbook

### Running the application stand-alone

```bash
mvn clean verify
mvn spring-boot:run
```

### Dockerizing the application

Please follow the instructions of the README file that is located in the root folder of the project to run all dockerized microservices.

## End-points

Application runs on http://localhost:8100/ as a default configuration. This is an internal API that shouldn't be exposed.

### /account/register POST
Request Body: 
```json
{
  "name": "Keano van Dongen",
  "address": "3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG",
  "dob": "2020-10-10",
  "documentNr": "vbkpjcnchg6p",
  "userName": "keano"
}
```

Response of successful registration:
```
200 OK
```

```json
{
  "userName": "keano",
  "password": "xNb)f%1vR49RRO1c"
}
```

Response of failed registration with same userName:
```
409 Conflict
```
```json
{
  "message": "User name is in use"
}
```

Response of failed registration with missing userName:
```
400 Bad Request
```
```json
{
  "message": "userName: must not be null"
}
```
Response of failed registration with internal server error:
```
500 Internal Server Error
```
```json
{
  "message": "Internal server error"
}
```

---

Request Body of failed registration with wrong date format:
```json
{
  "name": "Keano van Dongen",
  "address": "3 hoog Gedeonaweg 637 II, Margarethaambacht, WI 9410 IG",
  "dob": "10-10-2020",
  "documentNr": "vbkpjcnchg6p",
  "userName": "keano"
}
```
Response of failed wrong date format:
```
400 Bad Request
```
```json
{
  "message": "JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String \"10-10-2020\": Failed to deserialize java.time.LocalDate: (java.time.format.DateTimeParseException) Text '10-10-2020' could not be parsed at index 0",
}
```

### /account/logon POST
Request Body:
```json
{
  "userName": "keano",
  "password": "c4J0p%qA8JSy1Hj^"
}
```

Response of successful registration:
```
204 No Content
```

```
No response body
```

Response of failed registration with wrong credentials:
```
401 Unauthorized
```

```
No response body
```
---

Request Body of failed login with missing credentials:
```
No request body
```

Response of failed login with missing credentials:
```
400 Bad Request
```

```json
{
  "message": "password: must not be null, userName: must not be null"
}
```
