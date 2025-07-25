# OAuth 2.0 és OIDC használata

## Alkalmazás bemutatása - frontend

- Spring Boot alkalmazás, `pom.xml`
- Spring Data JPA, Spring MVC, RestController
- Alkalmazás elindítása
- Felület
- `application.yaml`
- Liquibase
- Felépítése: entity, repo, service, resource, controller
- Thymeleaf templates
- DataSource

# KeyCloak indítása és konfigurálása

```shell
docker run -d -p 8090:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin --name keycloak quay.io/keycloak/keycloak start-dev
```

- `http://localhost:8090` címen elérhető, `admin` / `admin`
- Létre kell hozni egy Realm-et (`EmployeesRealm`)
- Létre kell hozni egy klienst, amihez meg kell adni annak azonosítóját, <br /> és hogy milyen url-en érhető el (`employees-frontend`)
  - Ellenőrizni a _Valid Redirect URIs_ értékét
- Létre kell hozni a szerepköröket (`employees_user`)
- Létre kell hozni egy felhasználót (a _Email Verified_ legyen _On_ értéken, hogy be lehessen vele jelentkezni), beállítani a jelszavát (a _Temporary_ értéke legyen _Off_, hogy ne kelljen jelszót módosítani), <br /> valamint hozzáadni a szerepkört a _Role Mappings_ fülön (`johndoe`)

## KeyCloak URL-ek

> Figyelem: Az összes URL-ből eltávolítandó az `/auth` rész!

- Konfiguráció leírása

```
http://localhost:8090/realms/EmployeesRealm/.well-known/openid-configuration
```

- Tanúsítványok

```
http://localhost:8090/realms/EmployeesRealm/protocol/openid-connect/certs
```

- Token lekérése Resource owner password credentials használatával

```shell
curl -s --data "grant_type=password&client_id=employees-frontend&username=johndoe&password=johndoe" http://localhost:8090/realms/EmployeesRealm/protocol/openid-connect/token | jq
```

```http
POST http://localhost:8090/realms/EmployeesRealm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=employees-frontend&username=johndoe&password=johndoe
```

- A https://jws.io címen ellenőrizhető

## Frontend mint Client

- Függőség:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

```java
package employees;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(registry -> registry
                                .requestMatchers( "/create-employee")
                .authenticated()
//                                .hasRole("employee_admin")
                                .anyRequest()
                                .permitAll()
                        )
                .oauth2Login(Customizer.withDefaults())
                .logout(conf -> conf.
                                logoutSuccessUrl("/")
                        );
        return http.build();
    }

}
```

`application.yaml`

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: employees-frontend
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/login/oauth2/code/
            scope: openid,email,profile
        provider:
          keycloak:
            issuer-uri: http://localhost:8090/realms/EmployeesRealm
```

- `EmployeesController`

```java
@GetMapping("/")
public ModelAndView listEmployees(Principal principal) {
    log.debug("Principal: {}", principal);
```

`OAuth2AuthenticationToken`

- Frontend újraindítás után is bejelentkezve marad

- Logout: `http://localhost:8090/realms/EmployeesRealm/protocol/openid-connect/logout?redirect_uri=http://localhost:8080`
- Account Management: `http://localhost:8090/realms/EmployeesRealm/account`

## Access token továbbítása a backend felé

`ClientConfig`

```java
@Configuration
@EnableConfigurationProperties(EmployeesProperties.class)
public class ClientConfig {
    @Bean
    public EmployeesClient employeesClient(RestClient.Builder builder, OAuth2AuthorizedClientManager authorizedClientManager, EmployeesProperties employeesProperties) {
        var requestInterceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

        var webClient = builder
                .baseUrl(employeesProperties.getBackendUrl())
                .requestInterceptor((request, body, execution) -> {
                    clientRegistrationId("keycloak").accept(request.getAttributes());
                    return execution.execute(request, body);
                })
                .requestInterceptor(requestInterceptor)
                .build();
        var factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(webClient))
                .build();
        return factory.createClient(EmployeesClient.class);
    }
}
```

- Backend:

```java
@GetMapping
public List<EmployeeResource> listEmployees(@RequestHeader HttpHeaders headers) {
    log.debug("Headers: {}", headers);
    return employeesService.listEmployees();
}
```

```plain
Headers: [accept-encoding:"gzip", user-agent:"ReactorNetty/1.1.12", host:"localhost:8081", accept:"*/*", authorization:"Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICItcHJuVjJOWFk5ZjBlYnR4VDRySzdQRHo3X0NoMjc0WkhjbHVwejV6dDFZIn0.eyJleHAiOjE3MDE3MDMyMjMsImlhdCI6MTcwMTcwMjkyMywiYXV0aF90aW1lIjoxNzAxNzAxOTIxLCJqdGkiOiIyMzg1MjQzOC1hMDg0LTRjMDItODJmNi0wY2RlOGU3ODgzOTgiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwOTAvYXV0aC9yZWFsbXMvRW1wbG95ZWVzUmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNmNlNTcyNmItMDc0Mi00M2RjLWJkNDYtYjAwOWExYmFjZWI5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZW1wbG95ZWVzLWZyb250ZW5kIiwibm9uY2UiOiIyWERGeU80ZHlXVjl1THd2WHJQU2E3U09Lb1djVjZURU44cVRBM2JBZmI0Iiwic2Vzc2lvbl9zdGF0ZSI6ImI1MDY4NmViLThkZTgtNDkxYS05MGZhLWFlZGY1NjgzOTU0NiIsImFjciI6IjAiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRlZmF1bHQtcm9sZXMtZW1wbG95ZWVzcmVhbG0iLCJlbXBsb3llZXNfdXNlciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJzaWQiOiJiNTA2ODZlYi04ZGU4LTQ5MWEtOTBmYS1hZWRmNTY4Mzk1NDYiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiam9obmRvZSJ9.NmXHCLgus0vQWnUHK2LlJeHGfBT5X_jneNHjlm9PRT6qHqMF17rMiZXuVSoLewSK3oRATg_7qYH7Gcj0jzJxG8WNeJDp9tIVngd-S_KUGggssJpxHPUDVgY_clI7uQTbhPR6bz1Ye05Pf68M9XpRPkWsin9P73vdsBJ5jOCUioob-zbEkrB7uGCA68MQsSKamdyR8anNun3fqhsqaktbnJtn65uJjIfnigmUixY70T2Ic9OVrNTSIbN8UxX5Gam-92R-Qx61AFJC57HOrVzD6CV-VrFMy7TgRfJRNBS1ty7akB8Ag-bMbSkPfj_Z1Z_f_rCUcVAUfvAq24D9ZwjaVA"]
```

## Backend mint Resource Server

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

```java
package employees;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(registry -> registry
                        .requestMatchers(HttpMethod.POST, "/api/employees")
                                .authenticated()
//                        .hasRole("employees_user")
                        .anyRequest()
                        .permitAll()
                )
                .oauth2ResourceServer(conf -> conf.jwt(Customizer.withDefaults()));
        return http.build();
    }

}
```

`application.yaml`

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8090/realms/EmployeesRealm
```

- `http` fájlból a `POST` kérés meghívásakor a következő választ kapjuk:

```json
{
  "timestamp": "2023-12-04T15:30:43.802+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/employees"
}
```

```java
@GetMapping
public List<EmployeeResource> listEmployees(@RequestHeader HttpHeaders headers, Principal principal) {
    log.debug("Principal: {}", principal);

```

```plain
JwtAuthenticationToken [Principal=org.springframework.security.oauth2.jwt.Jwt@28b3d686, Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=127.0.0.1, SessionId=null], Granted Authorities=[SCOPE_openid, SCOPE_profile, SCOPE_email]]
```
