package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import static java.util.stream.Collectors.toList;

//creo rest controller para enviar info deseada a los players
    @RestController
    @RequestMapping("/api")
    public class AppController {

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        PlayerRepository playerRepository;

        @Autowired
        SalvoRepository salvoRepository;

        @Autowired
        ShipRepository shipRepository;

        @Autowired
        GameRepository gameRepository;

        @Autowired
        GamePlayerRepository gamePlayerRepository;

        @Autowired
        ScoreRepository scoreRepository;

        // hago un request para solicitar info a los games
        @RequestMapping("/games")
        //crea lista de TODOS los games con funcion getGameAll
        public Map<String, Object> MakeAuthPlayerDTO(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap();

        if (Objects.isNull(authentication)){
            dto.put("player", "Guest");
            } else if(Objects.nonNull(playerAuth(authentication))){
                dto.put("player", playerAuth(authentication).makePlayerDTO());
            }
            dto.put("games", gameRepository.findAll()
                    .stream()
                    .map(game -> game.makeGameDTO())
                    .collect(toList()));
            return dto;
        }

        @RequestMapping("/game_view/{gamePlayerId}")
        //quiero obtener un gamePlayer segun su ID para obtener info de sus barcos
        public Map<String, Object> getGamePlayerDTO(@PathVariable Long gamePlayerId) {
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).get();

            //creo un dto para recopilar la data, recurro a gamerepo para encontrar segun su id al player,
            // obtener el dato desde la clase game que refiere al repo gameplayer
            // obteniendo el player y guardandolo en el dto
            Map<String, Object> dto = gameRepository.findById(gamePlayerId).get().makeGameDTO();
            //recuro a gameplayerrepo para encontrar la info del player especifico, obteniendo mediante el dto
            //makegameplayershipsdto la info de los ships mediante el dto makeshipsdto
            // y trayendo una lista de todos los barcos de este player
            dto.put("ships", gamePlayerRepository.findById(gamePlayerId).get().makeGamePlayerShipsDTO());
            //
            dto.put("salvoes", gamePlayer.getGame().getGamePlayers()
                .stream()
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvoes()
                        .stream()
                        .map(salvo -> salvo.makeSalvoDTO()))
                        .collect(toList()));
            return dto;
                }

        @RequestMapping("/leaderboard")
            List<Object> getPlayerAll() {
                return playerRepository.findAll()
                        .stream()
                        .map(Player::makeLeaderboardDTO)
                        .collect(toList());
            }

        @RequestMapping(path = "/players", method = RequestMethod.POST)
        public ResponseEntity<Object> register(
                @RequestParam String email, @RequestParam String password) {

            if (email.isEmpty() || password.isEmpty()) {
                return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
            }

            if (playerRepository.findByUsername(email) !=  null) {
                return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
            }

            playerRepository.save(new Player(email, passwordEncoder.encode(password)));
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        private boolean isGuest(Authentication authentication) {
            return authentication == null || authentication instanceof AnonymousAuthenticationToken;
        }

        private Player playerAuth(Authentication authentication){
            return playerRepository.findByUsername(authentication.getName());
        }
}

