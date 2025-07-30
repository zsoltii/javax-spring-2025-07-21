package employees;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;

    @GetMapping("/api/counter")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void count() {
        counterService.count();
        log.info("Response");
    }

    /**
     * returns with HTTP 200 OK status when the counter is finished.
     */
    @GetMapping("/api/counter-future")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<Void> counterFuture() {
        log.info("Start counter async (future)");
        return counterService.counterFuture();
    }
}
