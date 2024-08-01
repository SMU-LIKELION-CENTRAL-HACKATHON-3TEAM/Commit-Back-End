package com.likelion.commit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CommitApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommitApplication.class, args);
	}

}
