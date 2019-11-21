package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String username;

    @OneToMany(mappedBy = "player", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "player", fetch=FetchType.EAGER)
    private Set<Score> scores;

    public Player(){}


    public long getId() {
        return id;
    }

    public Player(String username){
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    //obtengo scores del player en todos los games
    public Set<Score> getScores() {
        return scores;
    }
    //obtengo el score del player en un game particular
    public Score getScore(Game game) {
        return this.getScores()
                .stream()
                .filter(score -> score.getGame().getId() == game.getId())
                .findFirst().orElse(null);
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }

    public Map<String, Object> makePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("email", this.getUsername());
        return dto;
    }

    public Map<String, Object> makeLeaderboardDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("username", this.getUsername());
        dto.put("scores", this.getTotalScores());
        dto.put("wins", this.getWins());
        dto.put("losses", this.getLosses());
        dto.put("ties", this.getTies());
        return dto;
    }

    public long getWins(){
        return this.getScores()
                .stream()
                .filter(score -> score.getScore() == 1).count();
    }

    public long getLosses(){
        return this.getScores()
                .stream()
                .filter(score -> score.getScore() == 0).count();
    }

    public long getTies(){
        return this.getScores()
                .stream()
                .filter(score -> score.getScore() == 0.5).count();
    }

    public Double getTotalScores(){
        return this.getScores()
                .stream()
                .mapToDouble(Score::getScore).sum();
    }
}
