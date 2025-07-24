package employees;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.schema.registry.client.EnableSchemaRegistryClient;

@SpringBootApplication
@EnableSchemaRegistryClient
public class EmployeesFrontendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeesFrontendApplication.class, args);
	}

}
