# API Gateway

## About The Project
This project is an API gateway for distributing load into internal microservices to provide good customer service to ABC bank users.

## Design
The API gateway, by design, does not contain business logic or interfere with error messages from internal services. 
It acts as a proxy, passing along error codes and successful responses from internal services, and is solely responsible 
for routing traffic. To direct registration information to identity services, it calculates the hash code of a username 
and forwards it to the appropriate identity server instance responsible for storing the segmented data.

## Runbook

### Running the application stand-alone

```bash
mvn clean verify
mvn spring-boot:run
```

### Dockerizing the application

Please follow the instructions of the README file that is located in the root folder of the project to run all dockerized microservices.

## End-points

Application runs on http://localhost:8080/ as a default configuration. This is an external API that should be publicly accessible.

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
