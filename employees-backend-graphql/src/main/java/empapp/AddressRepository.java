package empapp;

import empapp.dto.AddressDto;
import empapp.dto.AddressWithEmployeeId;
import empapp.dto.EmployeeDto;
import empapp.entity.Address;
import empapp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.stream.Stream;

public interface AddressRepository extends JpaRepository<Address, Long> {
//    @Query("select new empapp.dto.AddressDto(a.id, a.city) from Address a where a.employee.id = :id")
//    List<AddressDto> findAddressesByEmployee(Long id);

    @Query("select new empapp.dto.AddressWithEmployeeId(a.id, a.city, a.employee.id) from Address a where a.employee.id in :ids")
    Stream<AddressWithEmployeeId> findAddressesByEmployee(List<Long> ids);
}
