package employees;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("employees.cors")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * If you put hibernate validation annotations on the fields, you can use, just add the hibernate-validation to the pom.xml
 */
public class CorsProperties {
    private String mapping;
    private String[] allowedOrigins;
}
