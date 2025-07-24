package training.configclientdemo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "demo")
public class DemoProperties {

    private String prefix;
}
