package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository repository) {
		return (args) -> {
			// save a couple of players
			repository.save(new Player("mail1@gmail.com"));
			repository.save(new Player("mail2@gmail.com"));
			repository.save(new Player("mail3@gmail.com"));
			repository.save(new Player("mail4@gmail.com"));
			repository.save(new Player("mail5@gmail.com"));
		};
	}
}
