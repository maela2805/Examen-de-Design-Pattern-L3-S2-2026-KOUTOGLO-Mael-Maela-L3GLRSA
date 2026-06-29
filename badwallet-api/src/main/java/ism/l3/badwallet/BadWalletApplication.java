package ism.l3.badwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BadWalletApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadWalletApplication.class, args);
    }
}
