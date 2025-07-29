package employees;

import com.google.protobuf.Any;
import com.google.rpc.BadRequest;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.protobuf.StatusProto;
import jakarta.validation.ConstraintViolationException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

import java.util.List;

@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(EmployeeNotFoundException.class)
    public StatusException handleEmployeeNotFoundException(EmployeeNotFoundException exception) {
        Status status = Status.NOT_FOUND.withDescription(exception.getMessage()).withCause(exception);
        return status.asException();
    }

    @GrpcExceptionHandler(ConstraintViolationException.class)
    public StatusException handleConstraintViolationException(ConstraintViolationException exception) {
        List<BadRequest.FieldViolation> violations = exception.getConstraintViolations()
                .stream().map(violation ->
                        BadRequest.FieldViolation.newBuilder()
                                .setField(violation.getPropertyPath().toString())
                                .setDescription(violation.getMessage())
                                .build()).toList();

            BadRequest badRequest = BadRequest.newBuilder()
            .addAllFieldViolations(violations)
           .build();

        com.google.rpc.Status statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Status.INVALID_ARGUMENT.getCode().value())
            .setMessage("Validation failed")
            .addDetails(Any.pack(badRequest))
            .build();

        return StatusProto.toStatusException(statusProto);
    }

}
