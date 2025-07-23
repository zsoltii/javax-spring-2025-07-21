# Spring oktatás anyagai

## Ütemezés

- Spring Boot Developer Tools
- Spring Data JPA audit
- Hibernate Envers, Spring Data Envers
- Aszinkron végrehajtás és ütemezés
- Cache-elés, EHCache és Redis integráció
- HTTP cache-elés
- Server-sent events, WebSockets

## Segédletek

```shell
docker run -d -e POSTGRES_DB=employees -e POSTGRES_USER=employees -e POSTGRES_PASSWORD=employees -p 5432:5432 --name employees-postgres postgres
```

Teams:

https://teams.microsoft.com/meet/3912307217785?p=wWXsh9Rq2Bg3NnkQGn

```shell
docker run --name employees-redis -p 6379:6379 -d redis
```

Blog:

https://www.jtechlog.hu/

```shell
docker run -d -p 8090:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin --name keycloak quay.io/keycloak/keycloak start-dev
```
