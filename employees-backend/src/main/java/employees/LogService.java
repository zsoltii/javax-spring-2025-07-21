package employees;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogService {

    /**
     * At the first exception, the application will stop processing events, the next listener doesn't get the event.
     * @param employeeDto
     */
    @EventListener
    public  void onMessageEvent(EmployeeDto employeeDto) {
        log.info("Received Employee Created Event: {}", employeeDto);
    }
}
