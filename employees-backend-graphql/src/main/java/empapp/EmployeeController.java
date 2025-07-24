package empapp;

import empapp.dto.AddressDto;
import empapp.dto.EmployeeDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    private final AddressRepository addressRepository;

    @QueryMapping
    public List<EmployeeDto> employees() {
        return employeeRepository.findAllDto();

    }

    @SchemaMapping(value = "addresses",  typeName = "Employee")
    public List<AddressDto> addresses(EmployeeDto employee) {
        return addressRepository.findAddressesByEmployee(employee.id());
    }
}
