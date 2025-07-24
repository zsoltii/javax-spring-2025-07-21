package training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.schema.registry.EnableSchemaRegistryServer;

@SpringBootApplication
@EnableSchemaRegistryServer
public class EmployeesSchemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeesSchemaApplication.class, args);
    }

}
