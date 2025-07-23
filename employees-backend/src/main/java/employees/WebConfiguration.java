package employees;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (isNotEmpty() && hasAllowedOrigins()) {
            registry.addMapping(corsProperties.getMapping())
                    .allowedOrigins(corsProperties.getAllowedOrigins());
        }
    }

    private boolean isNotEmpty() {
        return !StringUtils.hasText(corsProperties.getMapping());
    }

    private boolean hasAllowedOrigins() {
        return corsProperties.getAllowedOrigins() != null
                && corsProperties.getAllowedOrigins().length > 0;
    }
}
