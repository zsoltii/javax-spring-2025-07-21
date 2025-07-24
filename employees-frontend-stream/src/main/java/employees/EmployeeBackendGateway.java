package employees;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;

@Gateway
@RequiredArgsConstructor
public class EmployeeBackendGateway {

    private final StreamBridge streamBridge;

    public void createEmployee(Employee employee) {
        streamBridge.send("employee-backend-command", employee);
    }
}
