package employees;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@Async
public class MessagesService {

    public void counter(SseEmitter emitter) throws InterruptedException, IOException {
        for (int i = 1; i <= 10; i++) {
            Thread.sleep(1000); // Simulate delay
            emitter.send("Message " + i);
        }
    }
}
