package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    public GamePlayer gamePlayer;

    private String shipType;
    @ElementCollection
    private List<String> locations = new ArrayList<>();

    public Ship(){};

    public Ship(String shipType, List<String> locations, GamePlayer gamePlayer){
        this.shipType = shipType;
        this.locations = locations;
        this.gamePlayer = gamePlayer;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public long getId() {
        return id;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<String> locations) {
        this.locations = locations;
    }
}
