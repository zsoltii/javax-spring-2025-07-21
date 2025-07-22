package employees;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public class MessagesService {

    @Async
    public void counter(SseEmitter emitter) throws InterruptedException, IOException {
        for (int i = 1; i <= 10; i++) {
            Thread.sleep(1000); // Simulate delay
            emitter.send("Message " + i);
        }
        emitter.complete();
    }

    public CompletableFuture<Void> counterCompletableFuture(SseEmitter emitter) {
        return CompletableFuture.runAsync(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    Thread.sleep(1000); // Simulate delay
                    emitter.send("CF Message " + i);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            emitter.complete();
        });
    }
}
