package employees;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record EmployeeDto(Long id, @NotBlank String name) implements Serializable {}
