package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.repositories.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;


    @RestController
    @RequestMapping("/api")
    public class AppController {

        @Autowired
        ShipRepository shipRepository;

        @Autowired
        GameRepository gameRepository;

        @Autowired
        GamePlayerRepository gamePlayerRepository;

        @RequestMapping("/games")
        public List<Object> getGameAll() {

            return gameRepository.findAll()
                    .stream()
                    .map(game -> game.makeGameDTO())
                    .collect(Collectors.toList());
        }

        @RequestMapping("/game_view/")
        public
}
