package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository repository, GameRepository gRepository, GamePlayerRepository gpRepository) {
		return (args) -> {
			// save a couple of players
            Player player_1 = repository.save(new Player("mail1@gmail.com"));
			Player player_2 = repository.save(new Player("mail2@gmail.com"));
            Player player_3 = repository.save(new Player("mail3@gmail.com"));
            Player player_4 = repository.save(new Player("mail4@gmail.com"));
            Player player_5 = repository.save(new Player("mail5@gmail.com"));
            Player player_6 = repository.save(new Player("mail6@gmail.com"));
			// save a couple of games

            Date dateGame1 = new Date();
            Game game_1 = new Game(dateGame1);
            Game game_2 = new Game(Date.from(dateGame1.toInstant().plusSeconds(3600)));
            Game game_3 = new Game(Date.from(dateGame1.toInstant().plusSeconds(7200)));

            gRepository.save(game_1);
            gRepository.save(game_2);
            gRepository.save(game_3);
            // save gameplayers
            GamePlayer gamePlayer_1 = gpRepository.save(new GamePlayer(player_1, game_1));
            GamePlayer gamePlayer_2 = gpRepository.save(new GamePlayer(player_2, game_1));
            GamePlayer gamePlayer_3 = gpRepository.save(new GamePlayer(player_3, game_2));
            GamePlayer gamePlayer_4 = gpRepository.save(new GamePlayer(player_4, game_2));
            GamePlayer gamePlayer_5 = gpRepository.save(new GamePlayer(player_5, game_3));
            GamePlayer gamePlayer_6 = gpRepository.save(new GamePlayer(player_6, game_3));
		};
	}
}
