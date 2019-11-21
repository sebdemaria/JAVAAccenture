package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.ManyToOne;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

//creo rest controller para enviar info deseada a los players
    @RestController
    @RequestMapping("/api")
    public class AppController {

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
        public List<Object> getGameAll() {

            return gameRepository.findAll()
                    .stream()
                    .map(game -> game.makeGameDTO())
                    .collect(toList());
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
}

