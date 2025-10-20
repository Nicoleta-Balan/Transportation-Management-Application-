package multitier.trans.Controllers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class HelloController {
    @GetMapping("/")
    public String hello() {
        return "Test pentru proiect 2";
    }

    @GetMapping("/time")
    public String time() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Timpul este: " + now.format(formatter);
    }
}