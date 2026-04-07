package siem.logquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("siem")
public class LogQueryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogQueryApplication.class, args);
    }
}
