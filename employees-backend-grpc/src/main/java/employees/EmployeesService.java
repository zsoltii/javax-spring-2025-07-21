package employees;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Validated
public class EmployeesService {

    private final EmployeesRepository repository;

    public List<EmployeeDto> listEmployees() {
        return repository.findAllResources();
    }

    public EmployeeDto findEmployeeById(long id) {
        return toDto(repository.findById(id).orElseThrow(notFountException(id)));
    }

    public EmployeeDto createEmployee(@Valid EmployeeDto command) {
        Employee employee = new Employee(command.name());
        repository.save(employee);
        return toDto(employee);
    }

    @Transactional
    public EmployeeDto updateEmployee(long id, EmployeeDto command) {
        Employee employee = repository.findById(id).orElseThrow(notFountException(id));
        employee.setName(command.name());
        return toDto(employee);
    }

    public void deleteEmployee(long id) {
        repository.deleteById(id);
    }

    private EmployeeDto toDto(Employee employee) {
        return new EmployeeDto(employee.getId(), employee.getName());
    }

    private Supplier<EmployeeNotFoundException> notFountException(long id) {
        return () -> new EmployeeNotFoundException("Employee not found with id: %d".formatted(id));
    }

}
