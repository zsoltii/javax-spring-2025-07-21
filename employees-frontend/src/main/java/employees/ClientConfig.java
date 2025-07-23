package employees;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Configuration
@EnableConfigurationProperties(EmployeesProperties.class)
public class ClientConfig {
    @Bean
    public EmployeesClient employeesClient(
            RestClient.Builder builder,
            EmployeesProperties employeesProperties,
            OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        OAuth2ClientHttpRequestInterceptor interceptor =
                new OAuth2ClientHttpRequestInterceptor(oAuth2AuthorizedClientManager);

        RestClient restClient =
                builder.baseUrl(employeesProperties.getBackendUrl())
                        .requestInterceptor(
                                (request, body, execution) -> {
                                    clientRegistrationId("keycloak")  // same as in application.properties spring.security.oauth2.client.registration.keycloak
                                            .accept(request.getAttributes());
                                    return execution.execute(request, body);
                                })
                        .requestInterceptor(interceptor)
                        .build();
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(EmployeesClient.class);
    }
}
