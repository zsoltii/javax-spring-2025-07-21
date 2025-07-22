package employees;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MessagesControll {

    private final MessagesService messagesService;

    private static final List<SseEmitter> SSE_EMITTERS = new ArrayList<>();

    @GetMapping("api/messages")
    @SneakyThrows
    public SseEmitter messages() {
        SseEmitter emitter = new SseEmitter();
        messagesService.counter(emitter);
        return emitter;
    }

    @GetMapping("api/messages-cf")
    @SneakyThrows
    public SseEmitter messagesCompletableFuture() {
        SseEmitter emitter = new SseEmitter();
        messagesService.counterCompletableFuture(emitter);
        return emitter;
    }

    @EventListener
    public void onMessageEvent(EmployeeDto employeeDto) {
        log.info("Received Employee Created Event message: {}", employeeDto);
        final List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : SSE_EMITTERS) {
            try {
                SseEmitter.SseEventBuilder event =
                        SseEmitter.event()
                                .data(employeeDto.name())
                                .comment("New employee created")
                                .id(String.valueOf(employeeDto.id()))
                                .name(employeeDto.getClass().getSimpleName());
                emitter.send(event.build());
            } catch (Exception e) {
                log.error("Error sending message to SSE emitter", e);
                deadEmitters.add(emitter);
            }
        }
        SSE_EMITTERS.removeAll(deadEmitters);
    }

    @GetMapping("/api/employees/messages")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter();
        SSE_EMITTERS.add(emitter);
        messagesService.connected(emitter);
        return emitter;
    }
}
