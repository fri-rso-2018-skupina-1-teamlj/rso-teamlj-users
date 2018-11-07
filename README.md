# RSO: Users microservice

## Prerequisites

```bash
docker run -d --name pg-users -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=userTable -p 5432:5432 postgres:10.5
docker run -d --name pg-users -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=userTable -p 5432:5432 --net=rso postgres:10.5
```

Local run (warning: debugger needs to be attached):
```
java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=y -jar api/target/users-api-1.0.0-SNAPSHOT.jar
```

```
docker build -t users:1.0 .
docker run -p 8080:8080 users:1.0
to change network host: docker run -p 8080:8080 --net=host users:1.0
```


Based on: https://github.com/jmezna/rso-customers
