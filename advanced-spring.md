# Bevezetés

## Alkalmazás indítása

- Meglévő alkalmazottakat nyilvántartó alkalmazást bővítek
- Projekt megnyitás, IntelliJ IDEA Ultimate
- Adatbázis indítás

```shell
docker run -d -e POSTGRES_DB=employees -e POSTGRES_USER=employees  -e POSTGRES_PASSWORD=employees  -p 5432:5432  --name employees-postgres postgres
```

- Swagger: http://localhost:8080/swagger-ui.html
- `src/test/http/employees.http`: List, Create, List

## Adatbázis kapcsolat felvétele

## Alkalmazás felépítése

- Háromrétegű Spring Boot alkalmazás, `pom.xml`
- `application.properties`, adatbázis séma generálás
- `Employee` entitás
- `EmployeeRepository` Spring Data JPA, Dynamic projection
- `EmployeeService`, MapStruct, `EmployeeDto` DTO
- `EmployeeController`

# Spring Data JPA Auditing

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Employee {

    @Id
    private Long id;

    private String name;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;
}
```

- `@EnableJpaAuditing` annotáció Java konfigurációnál konfigurációs osztályon

# AuditingEntityListener globális konfigurációja

`@EntityListeners(AuditingEntityListener.class)` törlése

`META-INF/orm.xml` állományban:

```xml
<entity-mappings>
    <persistence-unit-metadata>
        <persistence-unit-defaults>
            <entity-listeners>
            <entity-listener class="org.springframework.data.jpa.domain.support.AuditingEntityListener" />
            </entity-listeners>
        </persistence-unit-defaults>
    </persistence-unit-metadata>
</entity-mappings>
```

# Felhasználó feloldása és mentése

```java
@CreatedBy
private String createdBy;

@LastModifiedBy
private String lastModifiedBy;
```

Feloldás:

```java
@Component
public class StubAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("admin");
    }
}
```

Felhasználó feloldása <br /> Spring Security esetén:

```java
public class SpringSecurityAuditorAware implements AuditorAware<User> {

  public Optional<User> getCurrentAuditor() {
    return Optional.ofNullable(SecurityContextHolder.getContext())
			  .map(SecurityContext::getAuthentication)
			  .filter(Authentication::isAuthenticated)
			  .map(Authentication::getPrincipal)
			  .map(User.class::cast);
  }
}
```

# Auditing beágyazott osztállyal

```java
@Embeddable
public class AuditingMetadata {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;
}
```

```java
@Embedded
private AuditingMetadata auditingMetadata = new AuditingMetadata();
```

# Hibernate Envers

## Auditálás Hibernate Envers használatával

`pom.xml`

```xml
<dependency>
	<groupId>org.hibernate.orm</groupId>
	<artifactId>hibernate-envers</artifactId>
</dependency>
```

Entitás

```java
@Entity
@Audited
public class Employee {

    @Id
    private Long id;

}
```

Kapcsolódó entitások

```plaintext
Caused by: org.hibernate.MappingException: An audited relation
from empapp.Employee.addresses to a not audited entity empapp.Address!
```

`@NotAudited` a relációra, vagy `@Audited` a kapcsolódó entitásra

Táblák:

- `revinfo` közös tábla a revision tárolására
  - `rev` mező, revision azonosítója
  - `revtstmp` mező, a változás dátumának tárolására
- Entitás verzióinak tárolására az eredetivel megegyező tábla, `_AUD` posztfixszel
  - `rev` mező, külső kulcs a `revinfo` táblára
  - `revtype` mező, létrehozás (`0` - `ADD`), módosítás (`1` - `MOD`), törlés (`2` - `DEL`)

## Auditing adatok lekérdezése Spring Data Envers használatával

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-envers</artifactId>
</dependency>
```

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long>, RevisionRepository<Employee, Long, Long> {

    // ...
}
```

```java
@SpringBootTest
class EmployeeRepositoryIT {

    @Autowired
    EmployeeRepository employeeRepository;

    @Test
    void findRevisions() {
        var revisions = employeeRepository.findRevisions(1L);
        assertEquals("John Doe", revisions.getContent().get(0).getEntity().getName());
        assertEquals("Jack Doe", revisions.getContent().get(1).getEntity().getName());
        assertEquals(RevisionMetadata.RevisionType.DELETE, revisions.getContent().get(2).getMetadata().getRevisionType());

        var employee = employeeRepository.findRevision(1L, 2L);
        assertEquals("Jack Doe", employee.get().getEntity().getName());
    }

}
```

## Saját revision entitás

- Saját entitás osztály definiálása, két lehetőség:
  - Leszármazás a `DefaultRevisionEntity` osztálytól
  - Annotációkkal megjelölt mezők

```java
@Entity
@RevisionEntity(StubUsernameListener.class)
@Getter @Setter
public class EmployeeRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    private LocalDateTime revisionDate;

    private String username;
}
```

Listener:

```java
public class StubUsernameListener implements RevisionListener {

    @Override
    public void newRevision(Object o) {
        if (o instanceof EmployeeRevisionEntity revision) {
            revision.setUsername("admin");
        }
    }
}
```

# Aszinkron végrehajtás és ütemezés

## Deklaratív aszinkron végrehajtás

`empapp-async` könyvtár

- `@Configuration` annotációval ellátott osztályon `@EnableAsync` annotáció
- Metóduson `@Async` annotáció
- Visszatérési típus lehet `Future`, `ListenableFuture` vagy `CompletableFuture`

`Thread.sleep(Duration.of(1, ChronoUnit.SECONDS));`: Java 21

```java
@Service
@Slf4j
public class CounterService {

    @Async
    @SneakyThrows
    public void count() {
        for (int i = 1; i <= 10; i++) {
            log.info("Count: {}", i);
            Thread.sleep(Duration.of(1, ChronoUnit.SECONDS));
        }
    }
}
```

```java
@RestController
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;

    @GetMapping("/api/count")
    public void count() {
        counterService.count();
    }
}
```

`@EnableAsync` az application osztályon

## Aszinkron válasz státuszkód

```java
@ResponseStatus(HttpStatus.ACCEPTED)
```

## Deklaratív ütemezett végrehajtás

- `@Configuration` annotációval ellátott osztályon `@EnableScheduling` annotáció

```java
@Scheduled(fixedRate = 5000) // 5 másodpercenként
public void logCount() {
    // ...
}
```

```java
@Scheduled(cron = "*/10 * * * * ?") // 10 másodpercenként
public void logCount() {
    // ...
}
```

Cron formátum:

- `1` - Second (0–59)
- `2` - Minute (0–59)
- `3` - Hour (0–23)
- `4` - Day of month (1–31)
- `5` - Month (1–12 vagy JAN–DEC)
- `6` - Day of week (1–7 vagy SUN–SAT)
- `7` - Year (opcionális) (1970–2099)

Cron haladó:

- `*` - minden érték
- `6-10` - range
- `6,8,10` - list
- `*/10` - minden 10. másodpercben
- `?` - day of month vagy week (az egyik helyén, ha megadjuk a másikat)

## Ütemezés a Quartz használatával

`empapp-quartz`

`pom.xml`:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

```java
@AllArgsConstructor
public class EmployeeJob extends QuartzJobBean {

    private EmployeeService employeeService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        List<EmployeeDto> employees = employeeService.listEmployees();
        // String value = context.getMergedJobDataMap().getString("key1");
        System.out.println("Running cron job, employees " + employees.size());
    }
}
```

- Működik a Dependency Injection

```java
@Bean
public JobDetail buildJobDetail() {
    JobDetail jobDetail = JobBuilder.newJob(EmployeeJob.class)
            .withIdentity(UUID.randomUUID().toString(), "employees-job")
            .withDescription("Print employees Job")
            //.usingJobData("key1", "value1")
            .storeDurably()
            .build();
    return jobDetail;
}
```

Ha nincs `storeDurably()` hívás, kivétel.

Alapértelmezés szerint a Quartz csak akkor tárol egy job-ot az adatbázisban vagy memóriában, ha van hozzá tartozó trigger. Ha nincs trigger, akkor a job törlődik.

Ha a `storeDurably(true)` be van állítva, akkor a Quartz megtartja a job-ot, még akkor is, ha nincs trigger. Így később egy triggerrel újra lehet ütemezni.

```java
@Bean
public Trigger buildJobTrigger(JobDetail jobDetail) {
    return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("PrintEmployeesCountJob")
            .withDescription("Print employees Trigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("*/10 * * * * ?"))
            .build();
}
```

- Vagy `SimpleScheduleBuilder`

---

## Quartz adatbázis perzisztencia

`application.properties`:

```properties
spring.quartz.job-store-type=jdbc
spring.quartz.properties.org.quartz.jobStore.class=org.springframework.scheduling.quartz.LocalDataSourceJobStore
```

- SQL fájlok a `\org\quartz\impl\jdbcjobstore` könyvtárban
  - Flyway vagy Liquibase
- Quartz maga inicializálja

```properties
spring.quartz.jdbc.initialize-schema=always
```

- Postgres esetén

```conf
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
```

# Cache-elés

## Deklaratív, annotáció alapú cache-elés

`empapp-cache` könyvtár

Cache-elés beállítása:

```java
@Configuration
@EnableCaching
public class CacheConfig {

}
```

Alapesetben egy `ConcurrentMap` alapú implementáció.

Cache műveletek naplózása:

```properties
logging.level.org.springframework.cache=trace
```

Cache-elés metódus szinten:

```java
@Cacheable("employee")
public EmployeeDto findEmployeeById(long id) {
  // ...
}
```

- Kulcs: `Long`, érték: `EmployeeDto`

Evict:

You can also indicate whether the eviction should occur after (the default) or before the method is invoked by using the beforeInvocation attribute.

```java
@CacheEvict(value = "employee", key = "#id")
public EmployeeDto updateEmployee(long id, EmployeeDto command) {
}

@CacheEvict("employee")
public void deleteEmployee(long id) {
}
```

Evict helyett put:

```java
@CachePut(value = "employee", key = "#id")
// @CachePut(value = "employee", key = "#result.id")
public EmployeeDto updateEmployee(long id, EmployeeDto command) {
}
```

## Újabb cache bevezetése

```java
@Cacheable("employees")
public List<EmployeeDto> listEmployees() {
}
```

Kulcs: `SimpleKey[]`

Evict:

```java
@CacheEvict(value = "employees", allEntries = true)
public EmployeeDto createEmployee(EmployeeDto command) {
}
```

```java
@CacheEvict(value = "employees", allEntries = true)
public EmployeeDto updateEmployee(long id, EmployeeDto command) {
}

@Caching(evict = {
    @CacheEvict("employee"),
    @CacheEvict(value = "employees", allEntries = true)})
public void deleteEmployee(long id) {
}
```

Cache-elés személyre szabása:

- `keyGenerator`: kulcsgenerálás programozott módon `org.springframework.cache.interceptor.KeyGenerator` interfész implementációval
- `condition`: csak bizonyos feltétel mellett kerüljön be (Spring EL, pl `#page < 5`)
- `unless`: amikor a visszatérési érték nem cache-elhető (Spring EL)
- `sync`: szinkronizált, azaz a cache provider lockolja a cache entry-t. Nem minden provider támogatja.

- Összetett kulcs megadása EL-ben, több paraméterre hivatkozva: `{#id, #type}`

Konfiguráció megadása osztály szinten:

- Csak a konfiguráció, nem állít be cache-elést
- Összes annotációval ellátott metódusra vonatkozik
- `@CacheConfig` annotációval

## Programozott cache-elés `ConcurrentMap` implementációval

```java
// Injektálható
private CacheManager cacheManager;

public void evictSingleCacheValue(String cacheName, String cacheKey) {
    cacheManager.getCache(cacheName).evict(cacheKey);
}
```

## Cache eszközök összehasonlítása

- EHCache
  - Java-ban írt, library
  - In-memory, lokális cache
  - Memóriában tárol, de képes diskre is írni
  - Terracotta nélkül nem lehet elosztott cache-t implementálni
- Redis
  - Cache server, in-memory data store, key-value
  - HA
  - Hálózaton keresztül integrálható, bármilyen nyelven
  - Cache minták: https://redis.io/solutions/caching/
- Hazelcast
  - Elosztott, in-memory
  - Képes embedded módban, vagy különálló szolgáltatásként is futni

## Cache-elés Hazelcast használatával

`pom.xml`

```xml
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-spring</artifactId>
</dependency>
```

`application.yaml`

```yaml
cache:
  type: hazelcast
  hazelcast:
    config: classpath:hazelcast.yaml
```

`hazelcast.yaml`

```yaml
hazelcast:
  instance-name: dev-hz
  network:
    join:
      multicast:
        enabled: false
      tcp-ip:
        enabled: false
  map:
    default:
      time-to-live-seconds: 3600
      max-idle-seconds: 60
```

Elindítani egy második példányt a `8082` porton, és a cache át fog
szinkronizálódni. Ugyanazzal a clustername-mel egy subneten belül (ez auto-discovery, de explicit discovery is lehetséges), automatikusan klasztereződnek, és a .getMap()-pel elért map egy elosztott, replikált (backup count szerint) map lesz, amin a node-ok osztoznak.

## Cache-elés Redis használatával

Redis futtatása Dockerben:

```shell
docker run --name employees-redis -p 6379:6379 -d redis
```

`ehcache-redis` projekt

Függőségek és konfiguráció:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

- `Serializable` objektumok

Az `application.properties`:

```conf
spring.cache.cache-names=employees,employee
spring.cache.redis.time-to-live=10m
```

# HTTP cache-elés

## Last-Modified és If-Modified-Since

Spring Data JPA Auditinggal jól kombinálható

```java
@EnableJpaAuditing
```

```java
@EntityListeners(AuditingEntityListener.class)
public class Employee {

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;

}
```

```java
public record EmployeeDto(Long id, String name, LocalDateTime lastModifiedAt) {

}
```

```java
.lastModified(employee.lastModifiedAt().atZone(ZoneId.systemDefault()))
```

## ETag használata dinamikus tartalom esetén hashCode alapján

```java
.eTag(Integer.toString(employeeDto.hashCode()))
```

```http
< ETag : "-802699320"
```

```http
> If-none-match : "-802699320"

< HTTP/1.1 304 Not Modified
```

Idézőjelek között

## ETag használata dinamikus tartalom esetén version alapján

`Employee`:

```java
@Version
private int version;
```

`EmployeeDto`

```java
public record EmployeeDto(Long id, String name, int version) {

}
```

A táblákat törölni kell

```java
.eTag(Integer.toString(employeeDto.getVersion()))
```

## Lost update problem

```java
@PutMapping("/{id}")
@SuppressWarnings("unused")
public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable("id") long id,
                                    @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch,
                                    @RequestBody EmployeeDto command
        ) {
    EmployeeDto employeeDto = employeeService.findEmployeeById(id);
    int incomingVersion = Integer.parseInt(ifMatch.replaceAll("\"", ""));

    if (employeeDto.version() == incomingVersion) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, command));
    }
    else {
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
    }
}
```

`If-Match` headerben `*` is küldhető, ekkor mindenképp update-el

# Server-Send Events

## Server-Send Events válasz

`empapp-sse` könyvtár

Controller

```java
@GetMapping("/api/messages")
public SseEmitter getMessages() {
    SseEmitter emitter = new SseEmitter();
    // Ciklusban, késleltetetten, külön szálon
    messagesService.createMessages(emitter);
    return emitter;
}
```

## Server-Send Events publish and subscribe

Emitter elmentése

```java
private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

@GetMapping("/api/employees/messages")
public SseEmitter getMessages() {
    SseEmitter emitter = new SseEmitter();
    emitters.add(emitter);
    return emitter;
}
```

Küldés az emitternek

- Megszakadt kapcsolatok kezelésével

```java
@EventListener
public void employeeHasCreated(EmployeeHasCreatedEvent event) {
    List<SseEmitter> deadEmitters = new ArrayList<>();
    this.emitters.forEach(emitter -> {
        try {
            SseEmitter.SseEventBuilder builder = SseEmitter.event()
                    .name("message")
                    .comment("Employee has created")
                    .id(UUID.randomUUID().toString())
                    .reconnectTime(10_000)
                    .data(new Message("Employee has created: " + event.getName()));
            emitter.send(builder);
        }
        catch (Exception e) {
            deadEmitters.add(emitter);
        }
    });

    this.emitters.removeAll(deadEmitters);
}
```

Üzenet összeállítás

```java
SseEmitter.SseEventBuilder builder = SseEmitter.event()
        .name("message")
        .comment("Employee has created")
        .id(UUID.randomUUID().toString())
        .reconnectTime(10_000)
        // JSON marshal
        .data(new Message("Employee has created: " + event.getName()));
emitter.send(builder);
```

## Server-Send Events JavaScript kliens

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <title>Employees</title>
    <script src="js/employees.js"></script>
  </head>
  <body>
    <h1>Employees</h1>

    <div id="messages-div"></div>
  </body>
</html>
```

```javascript
window.onload = function () {
  const source = new EventSource("/api/messages");
  source.addEventListener("message", function (event) {
    const data = event.data;
    if (data === "Connected") {
      return;
    }
    const json = JSON.parse(data);
    const name = json.name;

    document.querySelector("#messages-div").innerHTML += `<p>${name}</p>`;
  });
};
```

# WebSocket kérés-válasz kommunikáció szerver oldal

![Architektúra](images/stomp.drawio.svg)

`empapp-websocket` könyvtár

WebSocket függőség

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

- Tomcat implementációt használja WebSocket kezelésre

Spring konfiguráció

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket-endpoint").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic");
    }

}
```

```java
public record RequestMessage(String requestText) {
}

public record ResponseMessage(String responseText) {
}

```

Spring controller

```java
@MessageMapping("/messages")
@SendTo("/topic/employees")
public Message sendMessage(MessageCommand command) {
    return new Message("Reply: " + command.getContent());
}
```

- `@MessageMapping` helyett `@SubscribeMapping` - csak a feliratkozó üzenetekre kerül meghívásra
- Alapesetben a `/topic/messages` (bejövő destination `/topic` prefix-szel) topicra válaszol, azonban ez felülírható a `@SendTo` annotációval

## WebSocket üzenet küldése JavaScript kliensből

JavaScript függőségek

```xml
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>stomp-websocket</artifactId>
    <version>2.3.4</version>
</dependency>

<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>sockjs-client</artifactId>
    <version>1.5.1</version>
</dependency>

<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>webjars-locator-core</artifactId>
</dependency>
```

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <title>Employees</title>
    <script src="/webjars/sockjs-client/sockjs.min.js"></script>
    <script src="/webjars/stomp-websocket/stomp.min.js"></script>
    <script src="/employees.js"></script>
  </head>
  <body>
    <h1>Employees</h1>

    <div id="message-div"></div>

    <form>
      <input id="message-input" />
      <button type="button" id="message-button">Send</button>
    </form>
  </body>
</html>
```

```javascript
window.onload = function () {
  const socket = new SockJS("/websocket-endpoint");
  const client = Stomp.over(socket);

  document.querySelector("#message-button").onclick = function () {
    const text = document.querySelector("#message-input").value;
    const json = JSON.stringify({ requestText: text });
    client.send("/app/messages", {}, json);
  };

  client.connect({}, function (frame) {
    client.subscribe("/topic/employees", function (message) {
      const body = message.body;
      const json = JSON.parse(body);
      const text = json.responseText;

      console.log(`Reply: ${text}`);
      document.querySelector("#message-div").innerHTML += `<p>${text}</p>`;
    });
  });
};
```

## WebSocket üzenetküldés üzleti logikából

```java
@Controller
public class WebSocketMessageController {

    private SimpMessagingTemplate template;

    // ...

    public void send() {
      // message létrehozása

      template.convertAndSend("/topic/employees", message); // JSON marshal
    }

}
```

## Spring for GraphQL

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.graphql</groupId>
    <artifactId>spring-graphql-test</artifactId>
    <scope>test</scope>
</dependency>
```

`application.yaml`

```yaml
graphql:
  graphiql:
    enabled: true
```

```graphql
schema {
  query: Query
}

type Address {
  city: String!
  id: ID!
}

type Employee {
  addresses: [Address!]!
  id: ID!
  name: String!
}

type Query {
  employees: [Employee!]!
}
```

```java
public record EmployeeDto(Long id, String name) {

}

public record AddressDto(Long id, String city) {
}
```

```java
@Controller
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final AddressRepository addressRepository;

    @QueryMapping
    public List<EmployeeDto> employees() {
        return employeeRepository.findAllDto();
    }

    @SchemaMapping
    public List<Address> addresses(Employee employee) {
        return addressRepository.findAddressByEmployee(employee);
    }
}
```

`EmployeeRepository`

```java
@Query("select new empapp.dto.EmployeeDto(e.id, e.name) from Employee e")
    List<EmployeeDto> findAllDto();
```

`AddressRepository`

```java
List<Address> findAddressByEmployee(Employee employee);
```

`http://localhost:8081/graphiql`

```graphql
{
  employees {
    id
    name
    addresses {
      id
      city
    }
  }
}
```

Batch

```java
@BatchMapping(typeName = "Employee", field = "addresses")
public Map<EmployeeDto, List<AddressDto>> addresses(List<EmployeeDto> employees) {
    List<Long> employeeIds = employees.stream()
            .map(EmployeeDto::id)
            .toList();
    List<Object[]> addressesByEmployeeId =
            addressRepository.findAllAddressesByEmployeeIds(employeeIds);
    Map<Long, List<AddressDto>> addressMap = addressesByEmployeeId.stream()
            .collect(Collectors.groupingBy(
                    arr -> (Long) arr[0],
                    Collectors.mapping(arr -> (AddressDto) arr[1], Collectors.toList())
            ));

    return employees.stream().collect(Collectors.toMap(
            e -> e,
            e -> addressMap.getOrDefault(e.id(), List.of())
    ));
}
```

`AddressRepository`

```java
@Query("select a.employee.id, new empapp.dto.AddressDto(a.id, a.city) from Address a where a.employee.id in :ids")
    List<Object[]> findAllAddressesByEmployeeIds(List<Long> ids);
```

# gRPC

Mivel a generált DTO-kra nem lehet Bean Validation annotációkat tenni,
ezért megmaradt a Service és a DTO réteg. Be kellett állítani, hogy a Service réteg
validáljon.

```java
@Bean
public MethodValidationPostProcessor methodValidationPostProcessor() {
  return new MethodValidationPostProcessor();
}
```

```java
@Validated
public class EmployeesService {

}
```

```java
public EmployeeDto createEmployee(@Valid EmployeeDto command) {
}
```

`EmployeesController`, `EmployeesExceptionHandler`, `Violation` törölhető

`pom.xml`

```xml
<dependency>
  <groupId>javax.annotation</groupId>
  <artifactId>javax.annotation-api</artifactId>
  <version>1.2</version>
</dependency>

<dependency>
  <groupId>net.devh</groupId>
  <artifactId>grpc-server-spring-boot-starter</artifactId>
  <version>3.1.0.RELEASE</version>
</dependency>
```

```xml
<build>
  <extensions>
    <extension>
      <groupId>kr.motd.maven</groupId>
      <artifactId>os-maven-plugin</artifactId>
      <version>1.7.0</version>
    </extension>
  </extensions>

  <plugins>
    <plugin>
      <groupId>org.xolstice.maven.plugins</groupId>
      <artifactId>protobuf-maven-plugin</artifactId>
      <version>0.6.1</version>
      <configuration>
        <protocArtifact>
          com.google.protobuf:protoc:3.19.4:exe:${os.detected.classifier}
        </protocArtifact>
        <pluginId>grpc-java</pluginId>
        <pluginArtifact>
          io.grpc:protoc-gen-grpc-java:1.45.0:exe:${os.detected.classifier}
        </pluginArtifact>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>compile</goal>
            <goal>compile-custom</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

  </plugins>
</build>
```

`src/main/proto/employees.proto`

```proto
syntax = "proto3";
option java_multiple_files = true;
package employees;
import "google/protobuf/wrappers.proto";
import "google/protobuf/empty.proto";

message EmployeeMessage {
  int64 id = 1;
  string name = 2;
}

message EmployeeMessageList {
  repeated EmployeeMessage users = 1;
}

service EmployeesService {
  rpc listEmployees(google.protobuf.Empty) returns (EmployeeMessageList);
  rpc findEmployeeById(google.protobuf.Int32Value) returns (EmployeeMessage);
  rpc createEmployee(EmployeeMessage) returns (EmployeeMessage);
  rpc updateEmployee(EmployeeMessage) returns (EmployeeMessage);
  rpc deleteEmployee(google.protobuf.Int32Value) returns (google.protobuf.Empty);
}
```

```java
package employees;

import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

@GrpcService
@RequiredArgsConstructor
public class EmployeesServiceController extends EmployeesServiceGrpc.EmployeesServiceImplBase {

    private final EmployeesService employeesService;

    @Override
    public void listEmployees(Empty request, StreamObserver<EmployeeMessageList> responseObserver) {
        var employees = employeesService.listEmployees().stream()
                .map(EmployeesServiceController::toMessage)
                .toList();
        var response = EmployeeMessageList.newBuilder().addAllUsers(employees).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void findEmployeeById(Int32Value request, StreamObserver<EmployeeMessage> responseObserver) {
        long id = request.getValue();
            var employee = toMessage(employeesService.findEmployeeById(id));
            responseObserver.onNext(employee);
            responseObserver.onCompleted();
    }

    @Override
    public void createEmployee(EmployeeMessage request, StreamObserver<EmployeeMessage> responseObserver) {
        EmployeeMessage employee = toMessage(employeesService.createEmployee(new EmployeeDto(request.getId(), request.getName())));

        responseObserver.onNext(employee);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void updateEmployee(EmployeeMessage request, StreamObserver<EmployeeMessage> responseObserver) {
        var id = request.getId();
        var updatedEmployee = toMessage(employeesService.updateEmployee(id,
                new EmployeeDto(request.getId(), request.getName())));
        responseObserver.onNext(updatedEmployee);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteEmployee(Int32Value request, StreamObserver<Empty> responseObserver) {
        var id = request.getValue();
        employeesService.deleteEmployee(id);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private static EmployeeMessage toMessage(EmployeeDto dto) {
        return EmployeeMessage.newBuilder().setId(dto.id()).setName(dto.name()).build();
    }

}
```

Kivételkezelés:

```java
@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(EmployeeNotFoundException.class)
    public StatusException handleEmployeeNotFoundException(EmployeeNotFoundException exception) {
        Status status = Status.NOT_FOUND.withDescription(exception.getMessage()).withCause(exception);
        return status.asException();
    }

    @GrpcExceptionHandler(ConstraintViolationException.class)
    public StatusException handleConstraintViolationException(ConstraintViolationException exception) {
        List<BadRequest.FieldViolation> violations = exception.getConstraintViolations()
                .stream().map(violation ->
                        BadRequest.FieldViolation.newBuilder()
                                .setField(violation.getPropertyPath().toString())
                                .setDescription(violation.getMessage())
                                .build()).toList();

            BadRequest badRequest = BadRequest.newBuilder()
            .addAllFieldViolations(violations)
           .build();

        com.google.rpc.Status statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Status.INVALID_ARGUMENT.getCode().value())
            .setMessage("Validation failed")
            .addDetails(Any.pack(badRequest))
            .build();

        return StatusProto.toStatusException(statusProto);
    }

}
```

Sajnos az IDEA HTTP Client nem jól kezeli, ezért pl. Kreya használható. A
gRPC endpoint legyen `http://localhost:9090`.
