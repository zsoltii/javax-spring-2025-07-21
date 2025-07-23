package employees;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record EmployeeDto(Long id, @NotBlank String name, int version, LocalDateTime lastModifiedAt) {

}
