package org.example.backend_tunisiahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;   // ← Ajoute cette ligne
@SpringBootApplication
@EnableAsync   // ← Ajoute cette annotation
public class BackendTunisiaHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendTunisiaHubApplication.class, args);
    }

}
