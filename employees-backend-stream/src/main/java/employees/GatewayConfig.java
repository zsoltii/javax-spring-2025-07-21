package employees;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class GatewayConfig {

    @Bean
    public Function<EmployeeDto, EmployeeDto> createEmployee(EmployeesService employeesService) {

        return e -> {
            log.info("Handling {}", e);
            return employeesService.createEmployee(e);
        };
    }
}
