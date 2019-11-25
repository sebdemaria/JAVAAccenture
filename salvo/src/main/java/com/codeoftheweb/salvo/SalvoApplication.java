package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

	@Bean
	public CommandLineRunner initData(PlayerRepository repository, GameRepository gRepository, GamePlayerRepository gpRepository, ShipRepository sRepository, SalvoRepository slRepository, ScoreRepository scrRepository) {
		return (args) -> {
			// save a couple of players
            Player player_1 = new Player("mail1@gmail.com", passwordEncoder().encode("pw1"));
			Player player_2 = new Player("mail2@gmail.com", passwordEncoder().encode("pw2"));
            Player player_3 = new Player("mail3@gmail.com", passwordEncoder().encode("pw3"));
            Player player_4 = new Player("mail4@gmail.com", passwordEncoder().encode("pw4"));
            Player player_5 = new Player("mail5@gmail.com", passwordEncoder().encode("pw5"));
            Player player_6 = new Player("mail6@gmail.com", passwordEncoder().encode("pw6"));

            repository.save(player_1);
            repository.save(player_2);
            repository.save(player_3);
            repository.save(player_4);
            repository.save(player_5);
            repository.save(player_6);


            // save a couple of games
            Date dateGame1 = new Date();
            Game game_1 = new Game(dateGame1);
            Game game_2 = new Game(Date.from(dateGame1.toInstant().plusSeconds(3600)));
            Game game_3 = new Game(Date.from(dateGame1.toInstant().plusSeconds(7200)));

            gRepository.save(game_1);
            gRepository.save(game_2);
            gRepository.save(game_3);
            //save gameplayers
            GamePlayer gamePlayer_1 = new GamePlayer(player_1, game_1);
            GamePlayer gamePlayer_2 = new GamePlayer(player_2, game_1);
            GamePlayer gamePlayer_3 = new GamePlayer(player_3, game_2);
            GamePlayer gamePlayer_4 = new GamePlayer(player_4, game_2);
            GamePlayer gamePlayer_5 = new GamePlayer(player_5, game_3);
            GamePlayer gamePlayer_6 = new GamePlayer(player_6, game_3);

            gpRepository.save(gamePlayer_1);
            gpRepository.save(gamePlayer_2);
            gpRepository.save(gamePlayer_3);
            gpRepository.save(gamePlayer_4);
            gpRepository.save(gamePlayer_5);
            gpRepository.save(gamePlayer_6);

            //Instance of locations
            List<String> locationsS1 = new ArrayList<>();
            locationsS1.add("H1");
            locationsS1.add("H2");
            locationsS1.add("H3");
            List<String> locationsS2 = new ArrayList<>();
            locationsS2.add("F1");
            locationsS2.add("F2");
            locationsS2.add("F3");
            List<String> locationsS3 = new ArrayList<>();
            locationsS3.add("C1");
            locationsS3.add("C2");
            locationsS3.add("C3");

            //save ships
            Ship ship_1 = new Ship("cruiser", locationsS1, gamePlayer_1);
            Ship ship_2 = new Ship("destroyer", locationsS2, gamePlayer_2);
            Ship ship_3 = new Ship("battleship", locationsS3, gamePlayer_3);

            sRepository.save(ship_1);
            sRepository.save(ship_2);
            sRepository.save(ship_3);

            //create locations array for salvoes parameter
            List<String> locationsSalvo1 = new ArrayList<>();
            locationsSalvo1.add("H1");
            locationsSalvo1.add("H2");
            List<String> locationsSalvo2 = new ArrayList<>();
            locationsSalvo2.add("F1");
            locationsSalvo2.add("F3");
            List<String> locationsSalvo3 = new ArrayList<>();
            locationsSalvo3.add("C2");
            locationsSalvo3.add("C3");

            //save salvoes
            List<Salvo> salvoes = new ArrayList<>();
            salvoes.add(new Salvo(gamePlayer_1, 1, locationsSalvo1));
            salvoes.add(new Salvo(gamePlayer_2, 1, locationsSalvo2));
            salvoes.add(new Salvo(gamePlayer_3, 1, locationsSalvo3));

            slRepository.saveAll(salvoes);

            //create & save finish dates for scores parameters
            Date finishDate1 = new Date();
            Date finishDate2 = new Date();
            Date finishDate3 = new Date();
            Date finishDate4 = new Date();
            Date finishDate5 = new Date();
            Date finishDate6 = new Date();

            //save scores
            List<Score> scores = new ArrayList<>();
            scores.add(new Score(game_1, player_1,0.5, finishDate1));
            scores.add(new Score(game_1, player_2,1, finishDate2));
            scores.add(new Score(game_2, player_3,0, finishDate3));
            scores.add(new Score(game_2, player_4,0, finishDate4));
            scores.add(new Score(game_3, player_5,1, finishDate5));
            scores.add(new Score(game_3, player_6,0.5, finishDate6));

            scrRepository.saveAll(scores);

		};
	}
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> {
            //busco player x username
            Player player = playerRepository.findByUsername(username);
            //si player no es null traigo info del user
            if (player != null) {
                return new User(player.getUsername(), player.getPassword(),
                        //establezco roll del user tanto admin como user
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                //si user es null tiro msg de user not found
                throw new UsernameNotFoundException("Unknown user: " + username);
            }
        });
    }
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //.antMatchers("/rest/**").hasAuthority("USER")
                .antMatchers("/*").permitAll()
                .and()
                .formLogin();

        http.formLogin()
                .usernameParameter("name")
                .passwordParameter("pwd")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}