package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
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

        if (Objects.isNull(authentication)) {
            dto.put("player", "Guest");
        } else if (Objects.nonNull(playerAuth(authentication))) {
            dto.put("player", playerAuth(authentication).makePlayerDTO());
        }
        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(toList()));
        return dto;
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "No autorizado"), HttpStatus.UNAUTHORIZED);
        }
        Game game = gameRepository.save(new Game());

        GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(playerAuth(authentication), game));

        return new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);
    }


    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long gameId, Authentication authentication) {
        Game game = gameRepository.findById(gameId).get();

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error","No autorizado"), HttpStatus.UNAUTHORIZED);
        }
        if (gameRepository.findById(gameId) == null) {
            return new ResponseEntity<>(makeMap("error","No existe este juego"), HttpStatus.FORBIDDEN);
        }
        if (gamePlayerRepository.findById(gameId).get().getPlayer().getGamePlayers().stream().count() == 2) {
            return new ResponseEntity<>(makeMap("error","el juego esta lleno"), HttpStatus.FORBIDDEN);
        }

        GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(playerAuth(authentication), game));

        return new ResponseEntity<>(makeMap("gpid", gamePlayer.getId()), HttpStatus.CREATED);
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    //quiero obtener un gamePlayer segun su ID para obtener info de sus barcos
    public ResponseEntity<Map<String, Object>> getGamePlayerDTO(@PathVariable Long gamePlayerId, Authentication authentication) {

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).get();
        Game game = gamePlayer.getGame();

        if (gamePlayer.getPlayer().getId() != playerAuth(authentication).getId()) {
            return new ResponseEntity<>(makeMap("error", "Acceso restringido"), HttpStatus.UNAUTHORIZED);
        }

        //creo un dto para recopilar la data, recurro a gamerepo para encontrar segun su id al player,
        // obtener el dato desde la clase game que refiere al repo gameplayer
        // obteniendo el player y guardandolo en el dto
        Map<String, Object> dto = gamePlayerRepository.getOne(gamePlayerId).getGame().makeGameDTO();
        //recuro a gameplayerrepo para encontrar la info del player especifico, obteniendo mediante el dto
        //makegameplayershipsdto la info de los ships mediante el dto makeshipsdto
        // y trayendo una lista de todos los barcos de este player
        // dto.put("ships", gamePlayerRepository.findById(gamePlayerId).get().makeGamePlayerShipsDTO());
        //
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers()
                .stream()
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvoes()
                        .stream()
                        .map(salvo1 -> salvo1.makeSalvoDTO()))
                .collect(toList()));
        dto.put("ships", gamePlayerRepository.getOne(gamePlayerId).makeGamePlayerShipsDTO());
        // dto.put("salvoes", new ArrayList<>());
        dto.put("gameState", getState(gamePlayer, gamePlayer.getOpponent()));
        dto.put("hits", this.hitDTO(gamePlayer));
        return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
    }

    private Map<String, Object> hitDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("self", gamePlayer.makeHitsDTO(gamePlayer.getOpponent()));
        dto.put("opponent", gamePlayer.getOpponent().makeHitsDTO(gamePlayer));
        return dto;
    }

    public void addScore(GamePlayer gamePlayer, GamePlayer gamePlayerSelf, GamePlayer gamePlayerOpponent){
        if (this.getState(gamePlayerSelf, gamePlayerOpponent).equals("WON")){
            scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getPlayer(),
                    1, new Date()));
        }else if (this.getState(gamePlayerSelf, gamePlayerOpponent).equals("WON")){
            scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getOpponent().getPlayer(),
                    1, new Date()));
        }
        if (this.getState(gamePlayerSelf, gamePlayerOpponent).equals("LOST")){
            scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getPlayer(),
                    0, new Date()));
        }else if (this.getState(gamePlayerSelf, gamePlayerOpponent).equals("LOST")){
            scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getOpponent().getPlayer(),
                    0, new Date()));
        }
        if (this.getState(gamePlayerSelf, gamePlayerOpponent).equals("TIE")){
            scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getPlayer(),
                    0.5, new Date()));
        }else if (this.getState(gamePlayerSelf, gamePlayerOpponent).equals("TIE")){
            scoreRepository.save(new Score(gamePlayer.getGame(), gamePlayer.getOpponent().getPlayer(),
                    0.5, new Date()));
        }

    }

    public boolean existsScore(Game game) {
        if (game.getScores().isEmpty()) {
            return false;
        }
        return true;
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> saveShips(@PathVariable Long gamePlayerId, @RequestBody List<Ship> ships, Authentication authentication) {

        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).get();

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayerRepository.findById(gamePlayerId).get().getId() != gamePlayerId) {
            return new ResponseEntity<>(makeMap("error", "El usuario no existe"), HttpStatus.UNAUTHORIZED);
        }

        if (playerAuth(authentication).getId() != gamePlayer.getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "El usuario no coincide"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.getShips().stream().count() >= 5) {
            return new ResponseEntity<>(makeMap("error", "Ya tienes barcos colocados"), HttpStatus.FORBIDDEN);
        }


        ships.stream().forEach(ship -> ship.setGamePlayer(gamePlayer));
        shipRepository.saveAll(ships);
        return new ResponseEntity<>(makeMap("OK", "Ships added"), HttpStatus.CREATED);
    }

    @RequestMapping("/games/players/{gamePlayerId}/salvoes")
    public ResponseEntity<Map<String, Object>> saveShips(@PathVariable Long gamePlayerId, @RequestBody Salvo salvoes, Authentication authentication) {

        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).get();

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayerRepository.findById(gamePlayerId).get().getId() != gamePlayerId) {
            return new ResponseEntity<>(makeMap("error", "El usuario no existe"), HttpStatus.UNAUTHORIZED);
        }

        if (playerAuth(authentication).getId() != gamePlayer.getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "El usuario no coincide"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.getSalvoes().stream().filter(salvo1 -> salvo1.getTurn() == salvoes.getTurn()).count() > 0) {
            return new ResponseEntity<>(makeMap("error", "Ya tienes salvoes colocados"), HttpStatus.FORBIDDEN);
        }

        salvoes.setTurn(gamePlayer.getSalvoes().size() + 1);
        salvoes.setGamePlayer(gamePlayer);

        salvoRepository.save(salvoes);

        return new ResponseEntity<>(makeMap("OK", "Salvoes added"), HttpStatus.CREATED);
    }


    @RequestMapping("/leaderboard")
    List<Object> getPlayerAll() {
        return playerRepository.findAll()
                .stream()
                .map(Player::makeLeaderboardDTO)
                .collect(toList());
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam String email, @RequestParam String password) {

        if (email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Missing data"), HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUsername(email).orElse(null) != null) {
            return new ResponseEntity<>(makeMap("error", "Name already in use"), HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(makeMap("OK", "Usuario creado"), HttpStatus.CREATED);
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    private Player playerAuth(Authentication authentication) {
        return playerRepository.findByUsername(authentication.getName()).get();
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public String getState(GamePlayer gamePlayerSelf, GamePlayer gamePlayerOpponent) {
        Game game = gamePlayerSelf.getGame();
        if (gamePlayerSelf.getShips().isEmpty()) {
            return "PLACESHIPS";
        }
        if (gamePlayerSelf.getGame().getGamePlayers().size() == 1) {
            return "WAITINGFOROPP";
        }
        if (gamePlayerOpponent.getSalvoes().size() == gamePlayerSelf.getSalvoes().size()
                && gamePlayerOpponent.totalHits() == gamePlayerOpponent.getShips()
                .stream().flatMap(ship1 -> ship1.getShipLocations().stream()).count())
            if (gamePlayerSelf.totalHits() == gamePlayerSelf.getShips()
                    .stream().flatMap(ship1 -> ship1.getShipLocations().stream()).count()){
                Score score  = new Score(game, gamePlayerSelf.getPlayer(),
                        0.5, new Date());
                Score score1  = new Score(game, gamePlayerOpponent.getPlayer(),
                        0.5, new Date());
                if (!this.existsScore(game)){
                    scoreRepository.save(score);
                    scoreRepository.save(score1);
                }
                return "TIE";
            } else {
                Score score  = new Score(game, gamePlayerSelf.getPlayer(),
                        1, new Date());
                Score score1  = new Score(game, gamePlayerOpponent.getPlayer(),
                        0, new Date());
                if (!this.existsScore(game)){
                    scoreRepository.save(score);
                    scoreRepository.save(score1);
                }
                {
                return "WON";
            }
    }
        if (gamePlayerOpponent.getSalvoes().size() == gamePlayerSelf.getSalvoes().size()
                && gamePlayerSelf.totalHits() == gamePlayerSelf.getShips().stream()
                .flatMap(ship1 -> ship1.getShipLocations().stream()).count()) {
            Score score = new Score(game, gamePlayerSelf.getPlayer(),
                    0, new Date());
            Score score1 = new Score(game, gamePlayerOpponent.getPlayer(),
                    1, new Date());

            if (!this.existsScore(game)){
                scoreRepository.save(score);
                scoreRepository.save(score1);
            }
            {
                return "LOST";
            }
        }

        if (gamePlayerSelf.getSalvoes().size() > gamePlayerOpponent.getSalvoes().size()) {
            return "WAIT";
        }
        if (gamePlayerSelf.getSalvoes().size() == gamePlayerOpponent.getSalvoes().size()) {
            if (gamePlayerSelf.getId() < gamePlayerOpponent.getId()) {
                return "PLAY";
            } else {
                return "WAIT";
            }
        }
        if (gamePlayerSelf.getSalvoes().size() < gamePlayerOpponent.getSalvoes().size()) {
            return "PLAY";
        }
        if (gamePlayerSelf.getSalvoes().size() == gamePlayerOpponent.getSalvoes().size()) {
            if (gamePlayerSelf.getId() > gamePlayerOpponent.getId()) {
                return "WAIT";
            } else {
                return "PLAY";
            }
        }
        return "LOST";
    }
}


