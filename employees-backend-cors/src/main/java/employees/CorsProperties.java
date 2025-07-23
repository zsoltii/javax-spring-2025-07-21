package employees;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "employees.cors")
@Data
public class CorsProperties {

    private String mapping;

    private String[] origins;
}
