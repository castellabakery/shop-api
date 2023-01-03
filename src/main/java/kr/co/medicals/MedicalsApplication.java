package kr.co.medicals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MedicalsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalsApplication.class, args);
    }

}
