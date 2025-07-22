package employees;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggerComponent {

    @EventListener
    public void log(EmployeeDto employee) {
        log.info("New employee created: {}", employee);
    }
}
