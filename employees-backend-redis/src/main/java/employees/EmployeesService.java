package employees;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class EmployeesService {

    private final EmployeesRepository repository;

    @Cacheable("employees")
    public List<EmployeeDto> listEmployees() {
        return repository.findAllResources();
    }

    @Cacheable("employee")
    public EmployeeDto findEmployeeById(long id) {
        return toDto(repository.findById(id).orElseThrow(notFountException(id)));
    }

//    @Caching(evict = {
            @CacheEvict(value = "employees", allEntries = true)
//            , @CacheEvict(value = "employee", key = "#id")
//    })
            @CachePut(value = "employee", key = "#id")
    @Transactional
    public EmployeeDto updateEmployee(long id, EmployeeDto command) {
        Employee employee = repository.findById(id).orElseThrow(notFountException(id));
        employee.setName(command.name());
        return toDto(employee);
    }

    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeDto createEmployee(EmployeeDto command) {
        Employee employee = new Employee(command.name().toUpperCase());
        repository.save(employee);
        return toDto(employee);
    }

    @Caching(evict = {
            @CacheEvict(value = "employees", allEntries = true),
            @CacheEvict(value = "employee")
    })
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
