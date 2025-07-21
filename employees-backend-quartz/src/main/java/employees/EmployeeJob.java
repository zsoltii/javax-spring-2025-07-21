package employees;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@RequiredArgsConstructor
@Slf4j
public class EmployeeJob extends QuartzJobBean {

    private final EmployeesService employeesService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
      log.info("Employees: {}", employeesService.listEmployees().size());
    }
}
