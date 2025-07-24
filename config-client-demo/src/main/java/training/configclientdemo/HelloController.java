package training.configclientdemo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableConfigurationProperties(DemoProperties.class)
@RequiredArgsConstructor
@Slf4j
public class HelloController {

    private final DemoProperties demoProperties;

    @GetMapping("/hello")
    public String hello() {
        log.debug("Hello world!");
        return demoProperties.getPrefix() + " World!";
    }


}
