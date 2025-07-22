package employees;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UpdateEmployeeVersionMissMatchException extends RuntimeException {
    private int currentVersion;
    private int expectedVersion;
}
