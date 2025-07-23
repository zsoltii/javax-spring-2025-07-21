package employees;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessagesController {

    @MessageMapping("/messages")
    @SendTo("/topic/employees")
    public ResponseMessage handleRequest(RequestMessage requestMessage) {
        return new ResponseMessage("Received: " + requestMessage.requestText());
    }
}
