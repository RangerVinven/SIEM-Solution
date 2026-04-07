package siem.utils;

import io.github.cdimascio.dotenv.Dotenv;

// Loads the env file
public class LoadEnvFile {
    public static void load() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}
