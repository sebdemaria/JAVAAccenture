package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToMany(mappedBy = "gamePlayer", fetch=FetchType.EAGER)
    private Set<Ship> ships;

    public GamePlayer(){
        this.joinDate = new Date();
    }

    public GamePlayer(Player player, Game game){
        this.player = player;
        this.game = game;
        this.joinDate = new Date();
    }

    public long getId() {
        return id;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public Ship addShip(String shipType, List<String> locations){
        Ship ship = new Ship(shipType, locations, this);
        return ship;
    }

    public Map<String, Object> makeGamePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        //le hasgo un map a player pq dentro de player estan los datos del mismo y con el dto los obtengo
        dto.put("player", this.getPlayer().makePlayerDTO());
        return dto;
    }

    //creo un dto para obtener la data de los ships creando una lista de estos datos
    // para luego enviarla a appcontroller
    public List<Object> makeGamePlayerShipsDTO() {
        //retorno get ships en forma de lista collected
        return this.getShips()
        //transformo datos de getships en stream
        .stream()
        //creo un map de los datos de shipdto
        .map(ship -> ship.makeShipDTO())
        //collect todos los datos de arriba en forma de lista
        .collect(Collectors.toList());

    }
}
