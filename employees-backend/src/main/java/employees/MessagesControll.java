package employees;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MessagesControll {

    private final MessagesService messagesService;

    @GetMapping("api/messages")
    @SneakyThrows
    public SseEmitter messages() {
        SseEmitter emitter = new SseEmitter();
        messagesService.counter(emitter);
        return emitter;
    }
}
