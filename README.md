# RSO: Users microservice

## Prerequisites

```bash
docker run -d --name pg-users -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=user -p 5432:5432 postgres:10.5
```

Based on: https://github.com/jmezna/rso-customers
