package employees;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeesRepository extends JpaRepository<Employee, Long> {

//    @Query("select new employees.EmployeeDto(e.id, e.name, e.version, e.updatedAt) from Employee e")
//    List<EmployeeDto> findAllResources();

    <T> List<T> findAllBy(Class<T> type);
}
