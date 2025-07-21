package employees;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CounterController {

    private final CounterService counterService;

    /**
     * immediately returns with HTTP 202 Accepted status.
     */
    @GetMapping("/api/counter")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void counter() {
        log.info("Start counter async");
        counterService.counter();
        log.info("Started counter async");
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
