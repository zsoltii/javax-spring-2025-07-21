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
    public EmployeesClient employeesClient(RestClient.Builder builder, EmployeesProperties employeesProperties,
                                           OAuth2AuthorizedClientManager authorizedClientManager) {
        var interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

        var restClient = builder
                .baseUrl(employeesProperties.getBackendUrl())

                .requestInterceptor(((request, body, execution) -> {
                    clientRegistrationId("keycloak").accept(request.getAttributes());
                    return execution.execute(request, body);
                }))

                .requestInterceptor(interceptor)
                .build();
        var factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(EmployeesClient.class);
    }
}
