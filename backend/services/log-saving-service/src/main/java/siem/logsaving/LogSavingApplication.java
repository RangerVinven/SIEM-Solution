package siem.logsaving;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("siem")
public class LogSavingApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogSavingApplication.class, args);
    }
}
