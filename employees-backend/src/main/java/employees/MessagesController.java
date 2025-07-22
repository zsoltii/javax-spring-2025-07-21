package employees;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessagesController {
    @MessageMapping("/messages")
    @SendTo("/topic/employees") // if no sendTo is specified, the response will be sent to the same destination as the request
    public ResponseMessage handleRequest(RequestMessage requestMessage) {
        return new ResponseMessage("received: " + requestMessage.requestText());
    }
}
