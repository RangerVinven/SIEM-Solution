package siem.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import siem.utils.LoadEnvFile;

@SpringBootApplication
@ComponentScan("siem")
public class AlertApplication {
    public static void main(String[] args) {
        LoadEnvFile.load();
        SpringApplication.run(AlertApplication.class, args);
    }
}
