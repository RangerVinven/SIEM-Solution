package siem.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import siem.utils.LoadEnvFile;

@SpringBootApplication(scanBasePackages = "siem")
public class AccountApplication {

	public static void main(String[] args) {
        LoadEnvFile.load();

		SpringApplication.run(AccountApplication.class, args);
	}

}
