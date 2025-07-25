# Spring Cloud bevezetés

# Spring Cloud Config

## Spring Cloud Config Server elindítása

- `config-server-demo`
- Lombok, Web
- `spring-cloud-config-server` függőség

```java
@EnableConfigServer
```

- `application.properties`

```properties
server.port=8888
spring.cloud.config.server.git.uri=file:///C:\\trainings\\javax-spcl2\\config
spring.cloud.config.server.git.default-label=master
```

- Git repo: `C:\trainings\javax-spcl2\config\config-client-demo.properties`

```properties
demo.prefix = Hello
config-clogging.level.training=debug
```

Ellenőrzés URL-en: `http://localhost:8888/config-client-demo/default`

## Spring Cloud Config Client elindítása

- `config-client-demo`
- `spring-boot-starter-web`, `spring-cloud-config-client`, `lombok`

```java
@Data
@ConfigurationProperties(prefix = "demo")
public class DemoProperties {

    private String prefix;
}
```

```java
@RestController
@AllArgsConstructor
@EnableConfigurationProperties(DemoProperties.class)
@Slf4j
public class HelloController {

    private DemoProperties demoProperties;

    @GetMapping("/api/hello")
    public Message hello() {
        log.debug("Hello called");
        return new Message(demoProperties.getPrefix() + name);
    }
}
```

- `application.properties`

```properties
spring.config.import=configserver:
spring.application.name=config-client-demo
```

# Spring Cloud Bus

## Kafka indítása

Apache Kafka is an open-source distributed event streaming platform.

`kafka/docker-compose.yaml`

## Konfiguráció újratöltése futásidőben

server:

`spring-cloud-config-monitor`, `spring-cloud-starter-bus-kafka` függőség

client:

- `spring-cloud-starter-bus-kafka` függőség

# Spring Cloud Stream

## Kafka üzenet küldése üzleti logikából

- Függőség: Stream, Kafka
- `CreateEmployeeCommand`

```java
@Service
@AllArgsConstructor
public class EmployeeBackendGateway {

    private StreamBridge streamBridge;

    public void createEmployee(Employee employee) {
        streamBridge.send("employee-backend-command",
                new CreateEmployeeCommand(employee.getName()));
    }

}
```

- Kafka topic

## Kafka üzenetfogadás és válasz

- Függőség: Stream, Kafka
- `CreateEmployeeCommand`
- `EmployeeHasBeenCreatedEvent`

```java
@Configuration(proxyBeanMethods = false)
@Slf4j
public class GatewayConfig {

    @Autowired
    private EmployeesService employeesService;

    @Bean
    public Function<CreateEmployeeCommand, EmployeeHasBeenCreatedEvent> createEmployee() {
        return command -> {
            var created = employeesService.createEmployee(new EmployeeResource(command.getName()));
            var event = new EmployeeHasBeenCreatedEvent(created.getId(), created.getName());
            log.debug("Event: {}", event);
            return event;
        };
    }
}
```

- Binding: binder hozza létre, kapcsolat a broker és a producer/consumer között. Mindig van neve.
  Alapértelmezetten: `<bean neve> + -in- + <index>`, vagy `out`. Hozzárendelhető a
  broker topic-ja.

```yaml
spring:
  cloud:
    stream:
      function:
        bindings:
          createEmployee-in-0: employee-backend-command
          createEmployee-out-0: employee-backend-event
```

- Ha csak egy `java.util.function.[Supplier/Function/Consumer]` van, akkor azt automatikusan bekonfigurálja,
  nem kell a `spring.cloud.function.definition` property. Azonban legjobb gyakorlat használni.

## Kafka üzenet fogadása

- `EmployeeHasBeenCreatedEvent`

```java
@Configuration(proxyBeanMethods = false)
@Slf4j
public class GatewayConfig {

    @Autowired
    private EmployeesService employeesService;

    @Bean
    public Consumer<EmployeeHasBeenCreatedEvent> employeeCreated() {
        return command -> log.debug("Event: {}", event);
    }
}
```

```yaml
spring:
  cloud:
    stream:
      function:
        bindings: employeeCreated-in-0:employee-backend-event
```

## Schema registry

Önálló Springes alkalmazás, REST API-val, H2 adatbázissal, JPA-val

Projekt neve: `stream-employees-schema-registry`

- Spring Web függőség
- `spring-cloud-stream-schema-registry-core` függőség a `pom.xml`-be

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-schema-registry-core</artifactId>
    <version>4.0.5</version>
</dependency>
```

- `@EnableSchemaRegistryServer` annotáció
- `application.properties`

```properties
server.port=8990
spring.application.name=schema-registry
```

## Avro formátumú üzenet küldése - frontend

- `pom.xml`

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-schema-registry-client</artifactId>
</dependency>
```

- Tranzitívan hivatkozik az Avrora

```xml
<plugin>
  <groupId>org.apache.avro</groupId>
  <artifactId>avro-maven-plugin</artifactId>
  <version>1.11.3</version>
  <configuration>
    <stringType>String</stringType>
  </configuration>
  <executions>
    <execution>
      <phase>generate-sources</phase>
      <goals>
        <goal>schema</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

- Application osztályon `@EnableSchemaRegistryClient` annotáció

- IDEA Avro Schema Support plugin
- `src/main/avro/CreateEmployeeCommand.avsc`

```json
{
  "type": "record",
  "name": "CreateEmployeeCommand",
  "namespace": "employees",
  "fields": [
    {
      "name": "name",
      "type": "string"
    }
  ]
}
```

- `src/main/avro/EmployeeHasBeenCreatedEvent.avsc`

```json
{
  "type": "record",
  "name": "EmployeeHasBeenCreatedEvent",
  "namespace": "employees",
  "fields": [
    {
      "name": "id",
      "type": "long"
    },
    {
      "name": "name",
      "type": "string"
    }
  ]
}
```

- `CreateEmployeeCommand`, `EmployeeHasBeenCreatedEvent` törlése
- `mvn clean package`, Maven frissítés
- `EmployeeBackendGateway`

Átírni a binding nevét:

```java
streamBridge.send("createEmployee", command);
```

- `application.yaml` fájlba beszúrni:

```yaml
spring:
  cloud:
    stream:
      bindings:
        createEmployee:
          contentType: application/*+avro
        employeeCreated-in-0:
          contentType: application/*+avro
```

Schema registry:

```http
### Schema registry
GET http://localhost:8990/createemployeecommand/avro
```

## Avro formátumú üzenet fogadása, válasz - backend

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-schema-registry-client</artifactId>
</dependency>
```

```xml
<plugin>
  <groupId>org.apache.avro</groupId>
  <artifactId>avro-maven-plugin</artifactId>
  <version>1.11.3</version>
  <configuration>
    <stringType>String</stringType>
  </configuration>
  <executions>
    <execution>
      <phase>generate-sources</phase>
      <goals>
        <goal>schema</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

- Application osztályon `@EnableSchemaRegistryClient` annotáció
- `.avsc` fájlok másolása
- `CreateEmployeeCommand`, `EmployeeHasBeenCreatedEvent` törlése
- `mvn clean package`, Maven frissítés
- `application.yaml` fájlba beszúrni:

```yaml
spring:
  cloud:
    function:
      definition: createEmployee
    stream:
      bindings:
        createEmployee-in-0:
          contentType: application/*+avro
        createEmployee-out-0:
          contentType: application/*+avro
```

- Avro formátumú üzenetek kezelése az IDEA Kafka pluginban

# Spring Cloud Circuit Breaker

- Absztrakciós réteg, támogatott implementációk:
  - Resilience4J
  - Spring Retry
- Támogatott mechanizmusok:
  - CircuitBreaker
  - Bulkhead
- Resilience4J
  - CircuitBreaker
  - Bulkhead
  - RateLimiter
  - Retry
  - TimeLimiter

## Backend alkalmazás előkészítése

- [Chaos Monkey for Spring Boot](https://codecentric.github.io/chaos-monkey-spring-boot/)

```xml
<dependency>
  <groupId>de.codecentric</groupId>
  <artifactId>chaos-monkey-spring-boot</artifactId>
  <version>3.1.0</version>
</dependency>
```

```yaml
spring:
  profiles:
    active: chaos-monkey
management:
  endpoint:
    chaosmonkey:
      enabled: true
```

```http
### Chaos Monkey állapotának lekérdezése
GET http://localhost:8081/actuator/chaosmonkey

### Chaos Monkey bekapcsolása
POST http://localhost:8081/actuator/chaosmonkey/enable
Content-Type: application/json

{
  "enabled": true
}

### Chaos Monkey - RestController watcher bekapcsolása
POST http://localhost:8081/actuator/chaosmonkey/watchers
Content-Type: application/json

{
  "restController": "true"
}


### Chaos Monkey - EmployeesController.listEmployees metódus dobjon kivételt
POST http://localhost:8081/actuator/chaosmonkey/assaults
Content-Type: application/json

{
  "level": 1,
  "latencyActive": false,
  "exceptionsActive": true,
  "exception": {
    "type": "java.lang.RuntimeException",
    "method": "<init>",
    "arguments": [
      {
        "type": "java.lang.String",
        "value": "Chaos Monkey - RuntimeException"
      }
    ]
  },
  "watchedCustomServices": ["employees.EmployeesController.listEmployees"]
}

### Chaos Monkey - EmployeesController.listEmployees metódus latency
POST http://localhost:8081/actuator/chaosmonkey/assaults
Content-Type: application/json

{
  "level": 1,
  "latencyActive": true,
  "latencyRangeStart": 1000,
  "latencyRangeEnd": 3000,
  "exceptionsActive": false,
  "exception": {
    "type": "java.lang.RuntimeException",
    "method": "<init>",
    "arguments": [
      {
        "type": "java.lang.String",
        "value": "Chaos Monkey - RuntimeException"
      }
    ]
  },
  "watchedCustomServices": ["employees.EmployeesController.listEmployees"]
}
```

## Resilience4j bevezetése, circuit breaker

Frontend projekt:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

## Retry

```java
@Retry(name = "clientRetry")
```

```yaml
resilience4j:
  retry:
    instances:
      clientRetry:
        max-attempts: 3
```

- Actuator

```http
###
GET http://localhost:8080/actuator/retries

###
GET http://localhost:8080/actuator/retryevents
```

# Spring Cloud Gateway

## Spring Cloud Gateway használatba vétele

- Függőségek: Gateway

```yaml
server:
  port: 8000

spring:
  cloud:
    gateway:
      routes:
        - id: employees
          uri: http://localhost:8081/
          predicates:
            - Path=/api/employees/**
```

## Header módosítása

```yaml
filters:
  - AddRequestHeader=X-Gateway, Hello
```

```
@RequestHeader HttpHeaders headers

log.debug("Headers: {}", headers);
```

# Service discovery és Eureka

## Eureka Service Discovery

Spring Cloud Eureka projekt létrehozása (`employees-eureka`)

- Netflix Eureka Server függőség
- `@EnableEurekaServer` annotáció

`application.yaml`

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-demo
```

`employees-backend` projekt módosítások

- `spring-cloud-starter-netflix-eureka-client` függőség

```xml
<properties>
    <spring-cloud.version>2022.0.4</spring-cloud.version>
</properties>
```

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

- Több példány indítása

`gateway-demo` projekt módosítások

- `spring-cloud-starter-netflix-eureka-client` függőség

## Spring Cloud Gateway load balancing

- Backend alkalmazásban átállni ip-címre

```yaml
eureka:
  instance:
    prefer-ip-address: true
```

- Gatewayen

- Eureka Client

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: employees-backend
          uri: lb://employees-backend
```
