package be.eurospacecenter.revise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReviseApplication {

	public static void main(String[] args) {SpringApplication.run(ReviseApplication.class, args);}

}
