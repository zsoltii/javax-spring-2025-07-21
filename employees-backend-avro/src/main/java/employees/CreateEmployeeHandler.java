package employees;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

//@Component("createEmployee")
@RequiredArgsConstructor
@Slf4j
public class CreateEmployeeHandler implements Function<EmployeeDto, EmployeeDto> {

    private final EmployeesService employeesService;

    @Override
    public EmployeeDto apply(EmployeeDto e) {
        log.info("Handling {}", e);
        return employeesService.createEmployee(e);
    }
}
