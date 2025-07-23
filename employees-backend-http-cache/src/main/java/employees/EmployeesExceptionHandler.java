package employees;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class EmployeesExceptionHandler {

    @ExceptionHandler
    public ProblemDetail handle(EmployeeNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler
    public ProblemDetail handle(MethodArgumentNotValidException exception) {
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, "Constraint Violation");
        List<Violation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map((FieldError fe) -> new Violation(fe.getField(), fe.getDefaultMessage()))
                .toList();
        problemDetail.setProperty("violations", violations);
        problemDetail.setProperty("error-id", UUID.randomUUID().toString());
        problemDetail.setType(URI.create("https://example.com/constraint-violation"));
        return problemDetail;
    }

    @ExceptionHandler
    public ProblemDetail handle(PreconditionFailedException exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED, exception.getMessage());
        problemDetail.setType(URI.create("https://example.com/precondition-failed"));
        return problemDetail;
    }

}
