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

    //en mappedby se pone el nombre de la variable privada que creaste en la otra clase que estas vinculando
    @OneToMany(mappedBy = "gamePlayer", fetch=FetchType.EAGER)
    private Set<Salvo> salvoes;

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


    public Set<Salvo> getSalvoes() {
        return salvoes;
    }

    public Score getScore(){
        return this.player.getScore(this.game);
    };

    public void setSalvoes(Set<Salvo> salvoes) {
        this.salvoes = salvoes;
    }
    public Ship addShip(String shipType, List<String> locations){
        Ship ship = new Ship(shipType, locations, this);
        return ship;
    }

    public List<String> setHitLocations(Salvo salvo){
        return this.getShips()
                .stream()
                .flatMap(ship -> ship.getShipLocations()
                        .stream()
                        .flatMap(shipLoc -> salvo
                            .getSalvoLocations()
                            .stream()
                            .filter(salvoLoc -> shipLoc.contains(salvoLoc))))
                .collect(Collectors.toList());
    }

    private long getShipHitsByTypeDTO(Set<Ship> ships, String shipType, Salvo salvo) {
        Ship ship = ships.stream().filter(ship1 -> ship1.getType().equals(shipType)).findFirst().get();
        return salvo.salvoHitAmount(ship);
    }

    private long getShipHitsDTO(Set<Ship> ships, String shipType, Salvo salvo) {
        Ship ship = ships.stream().filter(ship1 -> ship1.getType().equals(shipType)).findFirst().get();
        return this.getOpponent().getSalvoes().stream().filter(salvo1 -> salvo1.getTurn() <= salvo.getTurn())
                .map(salvo1 -> salvo1.salvoHitAmount(ship)).reduce(Long::sum).get();
    }

    private long getShipHitsTotalDTO(Set<Ship> ships, String shipType) {
        Ship ship = ships.stream().filter(ship1 -> ship1.getType().equals(shipType)).findFirst().get();
        return this.getOpponent().getSalvoes().stream().mapToLong(salvo1 -> salvo1.salvoHitAmount(ship)).sum();
    }

    public long totalHits(){
        if (ships.isEmpty()){return -1;}
        List<Long> totalHits = new ArrayList();
        List<Salvo> salvos = new ArrayList<>(this.getOpponent().getSalvoes());

        totalHits.add(this.getShipHitsTotalDTO(ships,"carrier"));
        totalHits.add(this.getShipHitsTotalDTO(ships, "battleship"));
        totalHits.add(this.getShipHitsTotalDTO(ships, "submarine"));
        totalHits.add(this.getShipHitsTotalDTO(ships, "destroyer"));
        totalHits.add(this.getShipHitsTotalDTO(ships, "patrolboat"));

        long sum = totalHits.stream().reduce(Long::sum).get();

        return sum;
    }

    public Map<String, Object> HitsByTurnDTO(Set<Ship> ships, Salvo salvo, Set<Salvo> salvos){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("carrierHits", this.getShipHitsByTypeDTO(ships, "carrier", salvo));
        dto.put("battleshipHits", this.getShipHitsByTypeDTO(ships, "battleship", salvo));
        dto.put("submarineHits", this.getShipHitsByTypeDTO(ships, "submarine", salvo));
        dto.put("destroyerHits", this.getShipHitsByTypeDTO(ships, "destroyer", salvo));
        dto.put("patrolboatHits", this.getShipHitsByTypeDTO(ships, "patrolboat", salvo));
        dto.put("carrier", this.getShipHitsDTO(ships,"carrier", salvo));
        dto.put("battleship", this.getShipHitsDTO(ships, "battleship", salvo));
        dto.put("submarine", this.getShipHitsDTO(ships, "submarine", salvo));
        dto.put("destroyer", this.getShipHitsDTO(ships, "destroyer", salvo));
        dto.put("patrolboat", this.getShipHitsDTO(ships, "patrolboat", salvo));
        return dto;
    }

    public Long missedHits(Salvo salvo){
        Long missed = 5 - this.setHitLocations(salvo).stream().count();

        return missed;
    }

    public Map<String, Object> makeGamePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        //le hasgo un map a player pq dentro de player estan los datos del mismo y con el dto los obtengo
        dto.put("player", this.getPlayer().makePlayerDTO());
        // if (this.getScore() != null){
        // dto.put("scores", this.getScore().getScore());}
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

    public GamePlayer getOpponent(){
        return this.getGame().getGamePlayers().stream()
                .filter(gamePlayer -> gamePlayer.getId() != this.getId())
                .findFirst()
                .orElse(new GamePlayer());
    }

    public List<Object> makeHitsDTO(GamePlayer gamePlayer) {
        List<Object> dtoList = new ArrayList<>();
        if (gamePlayer.getSalvoes() != null) {
            List<Salvo> salvosDelOpp = gamePlayer.getSalvoes().stream()
                    .sorted(Comparator.comparing(Salvo::getTurn)).collect(Collectors.toList());

            for (Salvo a: salvosDelOpp) {
                Map<String,Object> dto = new LinkedHashMap<>();
                dto.put("turn", a.getTurn());
                dto.put("hitLocations", this.setHitLocations(a));
                dto.put("damages", this.HitsByTurnDTO(ships, a, gamePlayer.getSalvoes()));
                dto.put("missed", this.missedHits(a));
                dtoList.add(dto);
            }

        }
        return dtoList;
    }
}
