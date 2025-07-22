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
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessagesController {

    private final MessagesService messagesService;

    private List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("api/messages")
    public SseEmitter messages() {
        SseEmitter emitter = new SseEmitter();
        messagesService.sendMessages(emitter);
        return emitter;
    }

    @EventListener
    public void onMessage(EmployeeDto employee) {
      log.info("New employee created: {}", employee);
      List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                                .id(UUID.randomUUID().toString())
                                .comment("New employee created")
                                .name(EmployeeDto.class.getSimpleName())
                                .data(employee);
                emitter.send(event);
            } catch (Exception e) {
//                log.error("Error sending message to emitter", e);
                deadEmitters.add(emitter);
            }
        }
      emitters.removeAll(deadEmitters);
    }

    @SneakyThrows
    @GetMapping("/api/employees/messages")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter();

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .id(UUID.randomUUID().toString())
                .comment("New employee created")
                .name(EmployeeDto.class.getSimpleName())
                .data("Connected");

        emitter.send(event);
        emitters.add(emitter);
        return emitter;
    }

}
