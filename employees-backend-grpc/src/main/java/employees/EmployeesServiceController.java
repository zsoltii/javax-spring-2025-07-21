package employees;

import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

@GrpcService
@RequiredArgsConstructor
public class EmployeesServiceController extends EmployeesServiceGrpc.EmployeesServiceImplBase {

    private final EmployeesService employeesService;

    @Override
    public void listEmployees(Empty request, StreamObserver<EmployeeMessageList> responseObserver) {
        var employees = employeesService.listEmployees().stream()
                .map(EmployeesServiceController::toMessage)
                .toList();
        var response = EmployeeMessageList.newBuilder().addAllUsers(employees).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void findEmployeeById(Int32Value request, StreamObserver<EmployeeMessage> responseObserver) {
        long id = request.getValue();
            var employee = toMessage(employeesService.findEmployeeById(id));
            responseObserver.onNext(employee);
            responseObserver.onCompleted();
    }

    @Override
    public void createEmployee(EmployeeMessage request, StreamObserver<EmployeeMessage> responseObserver) {
        EmployeeMessage employee = toMessage(employeesService.createEmployee(new EmployeeDto(request.getId(), request.getName())));

        responseObserver.onNext(employee);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void updateEmployee(EmployeeMessage request, StreamObserver<EmployeeMessage> responseObserver) {
        var id = request.getId();
        var updatedEmployee = toMessage(employeesService.updateEmployee(id,
                new EmployeeDto(request.getId(), request.getName())));
        responseObserver.onNext(updatedEmployee);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteEmployee(Int32Value request, StreamObserver<Empty> responseObserver) {
        var id = request.getValue();
        employeesService.deleteEmployee(id);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private static EmployeeMessage toMessage(EmployeeDto dto) {
        return EmployeeMessage.newBuilder().setId(dto.id()).setName(dto.name()).build();
    }

}
