package employees;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class EmployeesService {

    private final EmployeesRepository repository;

    public List<EmployeeDto> listEmployees() {
//        return repository.findAllResources();
        return repository.findAllBy(EmployeeDto.class);
    }

    public EmployeeDto findEmployeeById(long id) {
        return toDto(repository.findById(id).orElseThrow(notFountException(id)));
    }

    public EmployeeDto createEmployee(EmployeeDto command) {
        Employee employee = new Employee(command.name().toUpperCase());
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
        return new EmployeeDto(employee.getId(), employee.getName(), employee.getVersion(), employee.getLastModifiedAt());
    }

    private Supplier<EmployeeNotFoundException> notFountException(long id) {
        return () -> new EmployeeNotFoundException("Employee not found with id: %d".formatted(id));
    }

}
