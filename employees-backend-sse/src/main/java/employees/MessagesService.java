package employees;

import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class MessagesService {

    @SneakyThrows
    @Async
    public void sendMessages(SseEmitter emitter) {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000); // Simulate delay
            emitter.send("Message " + (i + 1));
        }
        emitter.complete();
    }
}
