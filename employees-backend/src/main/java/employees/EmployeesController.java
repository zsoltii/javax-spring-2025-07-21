package employees;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Slf4j
@RequiredArgsConstructor
public class EmployeesController {

    private final EmployeesService employeesService;

    @GetMapping
    public List<EmployeeDto> listEmployees(@RequestHeader HttpHeaders headers) {
        log.debug("Headers: {}", headers);
        return employeesService.listEmployees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> findEmployeeById(@PathVariable("id") long id) {
        final EmployeeDto employeeById = employeesService.findEmployeeById(id);
        return ResponseEntity.ok()
                .eTag(Integer.toString(employeeById.version()))
                .lastModified(employeeById.updatedAt().atZone(ZoneId.systemDefault()))
                .body(employeeById);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EmployeeDto> createEmployee(
            @Valid @RequestBody EmployeeDto employeeToCreate, UriComponentsBuilder builder) {
        var employee = employeesService.createEmployee(employeeToCreate);
        return ResponseEntity.created(
                        builder.path("/api/employees/{id}").buildAndExpand(employee.id()).toUri())
                .body(employee);
    }

    @PutMapping("/{id}")
    public EmployeeDto updateEmployee(
            @PathVariable("id") long id,
            @RequestBody EmployeeDto command,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        EmployeeDto existsEmployee = employeesService.findEmployeeById(id);
        int expectedVersion = Integer.parseInt(ifMatch.replaceAll("\"", ""));
        final int currentVersion = existsEmployee.version();
        if (expectedVersion != currentVersion) {
            throw new UpdateEmployeeVersionMissMatchException(currentVersion, expectedVersion);
        }
        return employeesService.updateEmployee(id, command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable("id") long id) {
        employeesService.deleteEmployee(id);
    }
}
