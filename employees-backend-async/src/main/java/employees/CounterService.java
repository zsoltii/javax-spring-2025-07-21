package employees;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CounterService {

    @SneakyThrows
    @Async
    // mikronaut esetén @ExecuteOn(TaskExecutors.BLOCKING) virtual thread indul
    // https://medium.com/@willitheowl/dont-write-your-micronaut-http-controllers-without-this-critical-annotation-29506c98d29d
    public void count() {
        for (int i = 0; i < 10; i++) {
            log.info("Counter: {}", i);
            Thread.sleep(Duration.ofSeconds(1).toMillis());
        }
    }

    // nem foglalja  thread pool szálat, hanem egy külön thread-en fut
    public CompletableFuture<Void> counterFuture() {
        return CompletableFuture.runAsync(this::count);
    }

    @Scheduled(cron = "*/5 * * * * ?")
    @SneakyThrows
    public void log() {
        Thread.sleep(Duration.ofSeconds(3).toMillis());
        log.info("Logging from CounterService");
    }
}
