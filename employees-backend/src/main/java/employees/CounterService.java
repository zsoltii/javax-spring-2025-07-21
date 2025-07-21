package employees;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@Async
public class CounterService {

    public static final Duration SLEEP_DURATION = Duration.ofSeconds(2);

    public void counter() {
        for(int i = 0; i < 10; i++) {
            try {
                Thread.sleep(SLEEP_DURATION.toMillis());
                log.info("waiting: {}s", SLEEP_DURATION.toSeconds() * (i + 1));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Thread was interrupted", e);
            }
        }
    }

    public CompletableFuture<Void> counterFuture() {
        return CompletableFuture.runAsync(this::counter);
    }
}
