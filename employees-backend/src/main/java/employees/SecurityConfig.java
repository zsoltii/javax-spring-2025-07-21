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
    public SecurityFilterChain springSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        registry ->
                                registry.requestMatchers(HttpMethod.POST, "/api/employees")
                                        .authenticated()
                                        .requestMatchers(HttpMethod.DELETE, "/api/employees/**")
                                        .authenticated()
                                        .requestMatchers(HttpMethod.PUT, "/api/employees/**")
                                        .authenticated()
                                        .anyRequest()
                                        .permitAll())
                .oauth2ResourceServer(conf -> conf.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
