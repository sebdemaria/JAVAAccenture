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

import java.util.*;


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

        @RequestMapping(path = "/games", method = RequestMethod.POST)
        public ResponseEntity<Object> createGame(Authentication authentication){
            if(isGuest(authentication)) {
                return new ResponseEntity<>("No autorizado", HttpStatus.UNAUTHORIZED);
            }

            Player player = playerRepository.findByUsername(authentication.getName()).orElse(null);

            Game game = gameRepository.save(new Game());

            GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(playerAuth(authentication), game));

            return new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);
        }


        @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
        public ResponseEntity<Object> joinGame(@PathVariable Long gameId, Authentication authentication){
            Game game = gameRepository.findById(gameId).get();

            if(isGuest(authentication)) {
                return new ResponseEntity<>("No autorizado", HttpStatus.UNAUTHORIZED);
            }
            if(gameRepository.findById(gameId) == null) {
                return new ResponseEntity<>("No existe este juego", HttpStatus.FORBIDDEN);
            }
            if(gamePlayerRepository.findById(gameId).get().getPlayer().getGamePlayers().stream().count() == 2){
                return new ResponseEntity<>("el juego esta lleno", HttpStatus.FORBIDDEN);
            }

            GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(playerAuth(authentication), game));

            return new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);
        }

        @RequestMapping("/game_view/{gamePlayerId}")
        //quiero obtener un gamePlayer segun su ID para obtener info de sus barcos
        public ResponseEntity<Map<String, Object>> getGamePlayerDTO(@PathVariable Long gamePlayerId, Authentication authentication) {

            if (isGuest(authentication)) {
                return new ResponseEntity<>(makeMap("Error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
            }

            GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).get();
            Game game = gamePlayer.getGame();

            if (gamePlayer.getPlayer().getId() != playerAuth(authentication).getId()) {
                return new ResponseEntity<>(makeMap("Error", "Acceso restringido"), HttpStatus.UNAUTHORIZED);
            }

            //creo un dto para recopilar la data, recurro a gamerepo para encontrar segun su id al player,
            // obtener el dato desde la clase game que refiere al repo gameplayer
            // obteniendo el player y guardandolo en el dto
            Map<String, Object> dto = gamePlayerRepository.getOne(gamePlayerId).getGame().makeGameDTO();
            Map<String, Object> hits = new LinkedHashMap<>();
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
            dto.put("ships", new ArrayList<>());
            dto.put("salvoes", new ArrayList<>());
            hits.put("self", new ArrayList<>());
            hits.put("opponent", new ArrayList<>());
            dto.put("hits",hits);
            return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
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
            return playerRepository.findByUsername(authentication.getName()).get();
        }

        private Map<String, Object> makeMap(String key, Object value) {
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            return map;
        }
}

