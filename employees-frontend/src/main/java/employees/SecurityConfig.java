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
        http.authorizeHttpRequests(
                        registry ->
                                registry.requestMatchers("/create-employee")
                                        .authenticated()
                                        .anyRequest()
                                        .permitAll())
                .oauth2Login(Customizer.withDefaults())
                .logout(conf -> conf.logoutSuccessUrl("/"));

        return http.build();
    }
}
