package siem.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import siem.utils.LoadEnvFile;

@EnableCaching
@SpringBootApplication
@ComponentScan("siem")
public class LogAggregatorApplication {

	public static void main(String[] args) {
        LoadEnvFile.load();
		SpringApplication.run(LogAggregatorApplication.class, args);
	}

}
