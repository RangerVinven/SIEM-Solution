package siem.normalisation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import siem.utils.LoadEnvFile;

@SpringBootApplication(scanBasePackages = "siem")
public class Application {

	public static void main(String[] args) {
        LoadEnvFile.load();
		SpringApplication.run(Application.class, args);
	}

}
