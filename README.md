# Spring oktatás anyagai

## Ütemezés

- Spring Boot Developer Tools
- Spring Data JPA audit
- Hibernate Envers, Spring Data Envers
- Aszinkron végrehajtás és ütemezés
- Cache-elés, EHCache és Redis integráció
- HTTP cache-elés
- Server-sent events, WebSockets
- Spring Security, OAuth 2, Keycloak
- CORS
- Spring for GraphQL
- Spring Cloud Config
- Spring Cloud Bus
- Spring Cloud Function
- Spring Cloud Stream, serialization formátumok (JSON, Protocol Buffers, Avro)
- Spring Cloud Circuit Breaker, Resilience4J
- Spring Cloud Gateway
- Service discovery és Eureka
- gRPC, Protocol Buffers

## Segédletek

```shell
docker run -d -e POSTGRES_DB=employees -e POSTGRES_USER=employees -e POSTGRES_PASSWORD=employees -p 5432:5432 --name employees-postgres postgres
```

```shell
docker run --name employees-redis -p 6379:6379 -d redis
```

```shell
docker run -d -p 8090:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin --name keycloak quay.io/keycloak/keycloak start-dev
```

## Források

### Blog

- [JTechLog](https://www.jtechlog.hu/)

### YouTube

- [Coffee + Software](https://www.youtube.com/@coffeesoftware)
- [Dan Vega](https://www.youtube.com/@DanVega)
- [SpringDeveloper](https://www.youtube.com/@SpringSourceDev)

### Könyvek

- [Clean Code: A Handbook of Agile Software Craftsmanship 1st Edition by Robert C. Martin (Author)](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)
- [The Clean Coder: A Code of Conduct for Professional Programmers (Robert C. Martin Series) 1st Edition](https://www.amazon.com/Clean-Coder-Conduct-Professional-Programmers/dp/0137081073)
- [Effective Java 3rd Edition by Joshua Bloch (Author)](https://www.amazon.com/Effective-Java-Joshua-Bloch/dp/0134685997)
- [Microservices Patterns: With examples in Java First Edition by Chris Richardson (Author)](https://www.amazon.com/dp/1617294543)
- [Microservices with Spring Boot 3 and Spring Cloud: Build resilient and scalable microservices using Spring Cloud, Istio, and Kubernetes 3rd Edition](https://www.amazon.com/Microservices-Spring-Boot-Cloud-microservices/dp/1805128698)
- [Designing Data-Intensive Applications: The Big Ideas Behind Reliable, Scalable, and Maintainable Systems 1st Edition](https://www.amazon.com/Designing-Data-Intensive-Applications-Reliable-Maintainable/dp/1449373321)
