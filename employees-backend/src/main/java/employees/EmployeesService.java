package employees;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class EmployeesService {

    public static final String EMPLOYEES = "employees";
    public static final String EMPLOYEE = "employee";
    private final EmployeesRepository repository;

    @Cacheable(EMPLOYEES)
    public List<EmployeeDto> listEmployees() {
        log.info("listEmployees");
        return repository.findAllResources();
    }

    @Cacheable(EMPLOYEE)
    public EmployeeDto findEmployeeById(long id) {
        return toDto(repository.findById(id).orElseThrow(notFountException(id)));
    }

    @CacheEvict(cacheNames = { EMPLOYEES }, allEntries = true)
    public EmployeeDto createEmployee(EmployeeDto command) {
        Employee employee = new Employee(command.name().toUpperCase());
        repository.save(employee);
        return toDto(employee);
    }

    @Caching(evict = {
            //            @CacheEvict(value = EMPLOYEE, key = "#id"),
            @CacheEvict(cacheNames = { EMPLOYEE, EMPLOYEES }, allEntries = true)
    })
    @CachePut(value = EMPLOYEE, key = "#id")
    @Transactional
    public EmployeeDto updateEmployee(long id, EmployeeDto command) {
        Employee employee = repository.findById(id).orElseThrow(notFountException(id));
        employee.setName(command.name());
        return toDto(employee);
    }


    @Caching(evict = {
            @CacheEvict(cacheNames = { EMPLOYEE, EMPLOYEES }, allEntries = true),
            @CacheEvict(value = EMPLOYEE)
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
