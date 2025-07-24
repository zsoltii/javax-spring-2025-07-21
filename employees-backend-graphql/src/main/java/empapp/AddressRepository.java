package empapp;

import empapp.dto.AddressDto;
import empapp.dto.EmployeeDto;
import empapp.entity.Address;
import empapp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("select new empapp.dto.AddressDto(a.id, a.city) from Address a where a.employee.id = :id")
    List<AddressDto> findAddressesByEmployee(Long id);
}
