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
}
