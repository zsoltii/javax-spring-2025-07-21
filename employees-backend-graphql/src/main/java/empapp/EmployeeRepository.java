package empapp;

import empapp.dto.EmployeeDto;
import empapp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("select new empapp.dto.EmployeeDto(e.id, e.name) from Employee e")
    List<EmployeeDto> findAllDto();
}
