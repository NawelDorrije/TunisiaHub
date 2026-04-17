package org.example.backend_tunisiahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // required for @Scheduled nightly repricing job
@EnableAsync        // required for @Async audit logging

public class BackendTunisiaHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendTunisiaHubApplication.class, args);
    }

}
