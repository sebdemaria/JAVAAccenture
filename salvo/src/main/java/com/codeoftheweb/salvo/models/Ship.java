package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    public GamePlayer gamePlayer;

    private String type;
    @ElementCollection
    private List<String> shipLocations = new ArrayList<>();

    public Ship(){};

    public Ship(String type, List<String> shipLocations, GamePlayer gamePlayer){
        this.type = type;
        this.shipLocations = shipLocations;
        this.gamePlayer = gamePlayer;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setShipLocations(List<String> shipLocations) {
        this.shipLocations = shipLocations;
    }

    public long getId() {
        return id;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getShipLocations() {
        return shipLocations;
    }

    public void setLocations(ArrayList<String> locations) {
        this.shipLocations = locations;
    }

    //creo un dto de ships para obtener toda la data de estos ships, location y type, para enviarla a gameplayer
    public Map<String, Object> makeShipDTO() {
        //el linkedhashmap me ordena los datos de type y location obtenidos
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", this.getType());
        dto.put("locations", this.getShipLocations());
        //retorno dto que es lo que almacena los datos de type y locations
        return dto;
    }
}
