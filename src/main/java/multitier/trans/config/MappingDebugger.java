package multitier.trans.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class MappingDebugger implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MappingDebugger.class);

    private final ApplicationContext applicationContext;

    public MappingDebugger(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        logger.info("------ REGISTERED ENDPOINTS START ------");
        mapping.getHandlerMethods().forEach((key, value) -> {
            if (key.toString().contains("/api/search")) {
                logger.info("{} -> {}", key, value);
            }
        });
        logger.info("------ REGISTERED ENDPOINTS END ------");
    }
}
