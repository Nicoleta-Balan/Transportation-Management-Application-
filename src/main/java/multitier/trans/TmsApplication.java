package multitier.trans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "multitier.trans")
@EnableScheduling
public class TmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TmsApplication.class, args);
    }
}
