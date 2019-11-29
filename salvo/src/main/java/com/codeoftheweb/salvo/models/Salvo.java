package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    // en joincolumn pones el nombre de la columna mediante la cual vincularias la clase
    // (casi siempre son las claves foraneas)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    private long turn;

    @ElementCollection
    @Column(name = "salvoLocation")
    private List<String> salvoLocations;

    public Salvo() {};

    public Salvo(GamePlayer gamePlayer, long turn, List<String> salvoLocations) {
        this.gamePlayer = gamePlayer;
        this.turn = turn;
        this.salvoLocations = salvoLocations;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Long getTurn() {
        return turn;
    }

    public void setTurn(Long turn) {
        this.turn = turn;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

    public List<String> setHitLocations(List<Ship> ships){
        List<String> hitLocations = new ArrayList<>();
            this.getSalvoLocations().stream().forEach(location -> ships.stream().forEach(ship -> {
                if (ship.getShipLocations().contains(location)){
                    hitLocations.add(location);
                }
            }));
        return hitLocations;
    }

    public List<String> shipHits(Ship ships) {
        List<String> hits = new ArrayList<>();
        this.getSalvoLocations()
                .stream()
                .filter(location -> ships.getShipLocations().contains(location))
                .count();
    }

    private Long getShipHitsByTypeDTO(List<Ship> ships, String shipType) {
        Ship ship = ships.stream().filter(ship1 -> ship1.getType().equals(shipType)).findFirst().get();
        this.shipHits(ship);
    }

    private Long getShipHitsDTO(List<Ship> ships, List<Salvo> salvos, String shipType) {
        this.
    }

    public Map<String, Object> HitsByTurnDTO(List<Ship> ships){
        Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("carrierHits", this.getShipHitsByTypeDTO(ships, "carrier"));
            dto.put("battleshipHits", this.getShipHitsByTypeDTO(ships, "battleship"));
            dto.put("submarineHits", this.getShipHitsByTypeDTO(ships, "submarine"));
            dto.put("destroyerHits", this.getShipHitsByTypeDTO(ships, "destroyer"));
            dto.put("patrolboatHits", this.getShipHitsByTypeDTO(ships, "patrolboat"));
            dto.put("carrier", this.getShipHitsDTO());
            dto.put("battleship", this.getShipHitsDTO());
            dto.put("submarine", this.getShipHitsDTO());
            dto.put("destroyer", this.getShipHitsDTO());
            dto.put("patrolboat", this.getShipHitsDTO());
            return dto;
    }

    public Map<String, Object> makeSalvoDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
                dto.put("turn", this.getTurn());
                dto.put("player", this.getGamePlayer().getPlayer().getId());
                dto.put("locations", this.getSalvoLocations());
                return dto;
    }

}
