package empapp;

import empapp.dto.AddressDto;
import empapp.dto.AddressWithEmployeeId;
import empapp.dto.EmployeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    private final AddressRepository addressRepository;

    @QueryMapping
    public List<EmployeeDto> employees() {
        return employeeRepository.findAllDto();

    }

//    @SchemaMapping(value = "addresses",  typeName = "Employee")
//    public List<AddressDto> addresses(EmployeeDto employee) {
//        return addressRepository.findAddressesByEmployee(employee.id());
//    }

    @BatchMapping(value = "addresses",  typeName = "Employee")
    @Transactional(readOnly = true)
    public Map<EmployeeDto, List<AddressDto>> addresses(List<EmployeeDto> employees) {
        List<Long> employeeIds = employees.stream()
                .map(EmployeeDto::id)
                .toList();

        Map<Long, List<AddressDto>> map  = addressRepository.findAddressesByEmployee(employeeIds)
                .collect(Collectors.groupingBy(AddressWithEmployeeId::employeeId,
                         Collectors.mapping(pair -> new AddressDto(pair.id(), pair.city()),
                                 Collectors.toList())
                        ));

        return employees.stream().collect(Collectors.toMap(
                Function.identity(),
                e -> map.getOrDefault(e.id(), List.of())));
    }

}
