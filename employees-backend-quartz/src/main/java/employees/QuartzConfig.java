package employees;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class QuartzConfig {

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob(EmployeeJob.class)
                .withIdentity("employee-job")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger jobTrigger(JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("EmployeesJobTrigger")
                .withDescription("Trigger for Employees Job")
                .withSchedule(CronScheduleBuilder.cronSchedule("*/10 * * * * ?"))
                .build();
    }
}
